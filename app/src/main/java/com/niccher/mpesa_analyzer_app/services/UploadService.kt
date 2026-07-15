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
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.helpers.DeviceFingerprint
import com.niccher.mpesa_analyzer_app.helpers.Encryptor
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.helpers.ProgressRequestBody
import com.niccher.mpesa_analyzer_app.interfaces.JsonUploadLoot
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class UploadService : Service() {

    private val kon = Konstants
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
    }

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

        // --- Query MPESA SMS ---
        val prefRead = context.getSharedPreferences(kon.SHARED_LAST_TIME, Context.MODE_PRIVATE)
        val value = prefRead.getString(kon.SHARED_LAST_TIME, "0")
        val lastUploadTime = value?.toLongOrNull() ?: 0L
        Log.i(kon.TAGGED, "Last upload watermark (ms)=$lastUploadTime")

        // ADDRESS typically "MPESA"; use UPPER-like match via LIKE for safety
        val selection =
            "${Telephony.Sms.DATE} > ? AND UPPER(${Telephony.Sms.ADDRESS}) LIKE ?"
        val selectionArgs = arrayOf(lastUploadTime.toString(), "%MPESA%")

        val cr: ContentResolver = context.contentResolver
        val cursor = try {
            Log.d(kon.TAGGED, "Querying SMS ContentResolver...")
            cr.query(Telephony.Sms.CONTENT_URI, null, selection, selectionArgs, null)
        } catch (se: SecurityException) {
            Log.e(kon.TAGGED, "SMS query SecurityException: ${se.message}", se)
            finishWithResult(false, "SMS permission blocked by system.")
            return
        }

        if (cursor == null) {
            Log.e(kon.TAGGED, "FAIL: SMS cursor null")
            finishWithResult(false, "Could not read SMS inbox.")
            return
        }

        cursor.use { c ->
            val totalSMS = c.count
            Log.i(kon.TAGGED, "MPESA SMS found since watermark: $totalSMS")

            if (!c.moveToFirst() || totalSMS == 0) {
                Log.i(kon.TAGGED, "No new SMS to upload")
                finishWithResult(true, "No new MPESA messages to upload.", 0)
                return
            }

            val sbsent = StringBuffer()
            for (j in 0 until totalSMS) {
                val smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                val smsNumber = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                // NO_WRAP: DEFAULT inserts newlines that break the embedded JSON on the server
                val smsBody = Base64.encodeToString(
                    c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)).toByteArray(),
                    Base64.NO_WRAP
                )
                val smsSeen = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SEEN))
                val smsThreadid = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                val smsId = c.getString(c.getColumnIndexOrThrow(Telephony.Sms._ID))

                val smsType = when (
                    c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)).toInt()
                ) {
                    Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
                    Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX -> "outbox"
                    Telephony.Sms.MESSAGE_TYPE_QUEUED -> "queued"
                    Telephony.Sms.MESSAGE_TYPE_DRAFT -> "draft"
                    else -> ""
                }
                sbsent.append(
                    "{\"Type\": \"$smsType\",\"Number\": \"$smsNumber\"," +
                        "\"Thread Id\": $smsThreadid,\"Date\": $smsDate," +
                        "\"Body\": \"$smsBody\",\"Seen\": $smsSeen,\"ID\": $smsId },-------(//)--------"
                )
                c.moveToNext()
            }

            val fileName = "sms_All_${System.currentTimeMillis()}"
            Log.i(kon.TAGGED, "Prepared $totalSMS messages, creating encrypted file: $fileName")
            makeEncryptedFile(context, fileName, sbsent, partToken, partDevId, totalSMS)
        }

        Log.i(kon.TAGGED, "=== UPLOAD PIPELINE FINISHED ===")
    }

    private fun cryptor(inputFile: File, inputStream: InputStream) {
        Encryptor.encodeToFile(
            kon.STRING_KEY,
            kon.STRING_KEY_SPEC,
            inputStream,
            FileOutputStream(inputFile)
        )
        Log.e(kon.TAGGED, "Cryptor: Encryption Completed")
    }

    private fun makeEncryptedFile(
        context: Context,
        fileName: String,
        dataSource: StringBuffer,
        partToken: String,
        partDevId: String,
        smsCount: Int
    ) {
        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput("${kon.STRING_PLAIN_FILE}$fileName", Context.MODE_PRIVATE)
            fos.write(dataSource.toString().toByteArray())

            val fileEncPlain = File(context.filesDir, "/${kon.STRING_PLAIN_FILE}$fileName")
            val fileEncAes = File(context.filesDir, "/${kon.STRING_ENC_AES_FILES}$fileName")

            val fileInputStream = BufferedInputStream(FileInputStream(fileEncPlain))
            cryptor(fileEncAes, fileInputStream)

            parserUpload(context, fileEncAes, fileName, partToken, partDevId, smsCount)
        } catch (e: FileNotFoundException) {
            Log.e(kon.TAGGED, "Error 1  ${e.message}")
            finishWithResult(false, "Could not create upload file: ${e.message}")
        } catch (e: IOException) {
            Log.e(kon.TAGGED, "Error 2  ${e.message}")
            finishWithResult(false, "IO error preparing upload: ${e.message}")
        } catch (e: Exception) {
            Log.e(kon.TAGGED, "Encrypt/prepare error: ${e.message}", e)
            finishWithResult(false, "Encrypt failed: ${e.message}")
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
        smsCount: Int
    ) {
        Log.i(kon.TAGGED, "Starting parserUpload: file=${files.name} size=${files.length()} bytes")

        val builder = NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setContentTitle("Uploading SMS")
            .setContentText("Upload in progress ($smsCount messages)")
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(true)
            .setProgress(100, 0, false)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        Log.i(kon.TAGGED, "Creating Retrofit service for upload")
        val service = ServiceGenerators.createService(JsonUploadLoot::class.java, context)

        val requestFile = ProgressRequestBody(files, "*/*".toMediaTypeOrNull()) { progress ->
            builder.setProgress(100, progress, false)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }

        val body = MultipartBody.Part.createFormData("varLoot", "$filename.txt", requestFile)
        val textType = "text/plain".toMediaTypeOrNull()
        val requestBody0 = partToken.toRequestBody(textType)
        val requestBody1 = partDevId.toRequestBody(textType)

        Log.i(kon.TAGGED, "Enqueueing upload request to server")
        val call = service.upload(requestBody0, requestBody1, body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
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

                try {
                    if (raw.isBlank()) {
                        finishWithResult(
                            false,
                            "Empty server response (HTTP ${response.code()})"
                        )
                        return
                    }

                    val jsonObject = org.json.JSONObject(raw)
                    val status = jsonObject.optInt("status", -1)
                    val message = jsonObject.optString(
                        "message",
                        jsonObject.optString("error", "Unknown error")
                    )

                    if (status == 1) {
                        Log.i(kon.TAGGED, "Upload SUCCESS: $message")
                        val encPlain = File(context.filesDir, "/${kon.STRING_PLAIN_FILE}$filename")
                        val encAes = File(context.filesDir, "/${kon.STRING_ENC_AES_FILES}$filename")
                        encPlain.delete()
                        encAes.delete()

                        prefs.getFileType(filename, System.currentTimeMillis().toString(), context)

                        val prefLootCount =
                            context.getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)
                        val currentCount = prefLootCount.getInt("loot_count", 0)
                        val newCount = currentCount + 1
                        // Use commit() to ensure write completes before broadcast
                        prefLootCount.edit().putInt("loot_count", newCount).commit()
                        Log.i(kon.TAGGED, "Incremented loot_count to $newCount")

                        finishWithResult(
                            true,
                            message.ifBlank { "Upload complete ($smsCount messages)" },
                            smsCount
                        )
                    } else {
                        Log.e(kon.TAGGED, "Upload FAILED by server: status=$status msg=$message")
                        finishWithResult(false, message.ifBlank { "Upload failed (status=$status)" })
                    }
                } catch (e: Exception) {
                    Log.e(kon.TAGGED, "Parse upload response error: ${e.message}", e)
                    finishWithResult(
                        false,
                        "Bad server response (HTTP ${response.code()}): ${raw.take(200)}"
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(kon.TAGGED, "Upload network error: ${t.message}", t)
                finishWithResult(false, "Network error: ${t.message}")
            }
        })
        Log.i(kon.TAGGED, "Upload request enqueued, waiting for response...")
    }

    private fun finishWithResult(success: Boolean, message: String, smsCount: Int = 0) {
        Log.i(kon.TAGGED, "Upload finish success=$success msg=$message count=$smsCount")

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
