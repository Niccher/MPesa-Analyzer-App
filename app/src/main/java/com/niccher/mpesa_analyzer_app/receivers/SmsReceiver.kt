package com.niccher.mpesa_analyzer_app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.niccher.mpesa_analyzer_app.database.AppDatabase
import com.niccher.mpesa_analyzer_app.database.TransactionEntity
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.helpers.MpesaParser
import com.niccher.mpesa_analyzer_app.helpers.SyncScheduler
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.services.UploadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender = message.displayOriginatingAddress ?: continue
                val body = message.displayMessageBody ?: continue
                val timestamp = message.timestampMillis

                Log.d("SmsReceiver", "Received SMS from: $sender")

                val normalizedSender = sender.uppercase().trim()
                if (normalizedSender.contains("MPESA") || 
                    normalizedSender.contains("AIRTELMONEY") || 
                    normalizedSender.contains("TKASH") ||
                    normalizedSender.contains("T-KASH")
                ) {
                    val parsed = MpesaParser.parseMessage(body)
                    if (parsed != null) {
                        val carrier = if (normalizedSender.contains("AIRTEL")) "Airtel" 
                                      else if (normalizedSender.contains("TKASH") || normalizedSender.contains("T-KASH")) "Telkom"
                                      else "Safaricom"
                        
                        val entity = TransactionEntity(
                            smsId = System.currentTimeMillis(),
                            sender = sender,
                            body = body,
                            amount = parsed.amount,
                            category = parsed.category,
                            direction = if (parsed.category == "Money Received") "incoming" else "outgoing",
                            carrier = carrier,
                            simSlot = 0,
                            timestamp = timestamp,
                            syncStatus = 0
                        )

                        val database = AppDatabase.getDatabase(context)
                        CoroutineScope(Dispatchers.IO).launch {
                            database.transactionDao().insertTransaction(entity)
                            Log.d("SmsReceiver", "Transaction saved to local DB.")

                            val mode = AppPrefs.getSyncMode(context)
                            var shouldUpload = false

                            when (mode) {
                                "immediate" -> {
                                    shouldUpload = true
                                }
                                "count" -> {
                                    val threshold = AppPrefs.getSyncCountThreshold(context)
                                    val watermark = context.getSharedPreferences(Constants.SHARED_LAST_TIME, Context.MODE_PRIVATE)
                                        .getLong(Constants.SHARED_LAST_SMS_ID, 0L)
                                    val unsyncedCount = SyncScheduler.getUnsyncedSmsCount(context, watermark)
                                    Log.d("SmsReceiver", "Count mode: unsyncedCount = $unsyncedCount, threshold = $threshold")
                                    if (unsyncedCount >= threshold) {
                                        shouldUpload = true
                                    }
                                }
                            }

                            if (shouldUpload) {
                                Log.d("SmsReceiver", "Triggering upload service...")
                                val uploadIntent = Intent(context, UploadService::class.java)
                                try {
                                    context.startService(uploadIntent)
                                } catch (e: Exception) {
                                    Log.e("SmsReceiver", "Failed to start UploadService from background receiver", e)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
