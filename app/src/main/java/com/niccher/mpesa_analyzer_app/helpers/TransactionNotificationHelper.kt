package com.niccher.mpesa_analyzer_app.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.niccher.mpesa_analyzer_app.MainActivity
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.database.TransactionEntity
import com.niccher.mpesa_analyzer_app.receivers.NotificationActionReceiver
import java.text.NumberFormat
import java.util.Locale

object TransactionNotificationHelper {

    const val CHANNEL_ID = "mpesa_realtime_transactions"
    private const val CHANNEL_NAME = "M-Pesa Real-Time Alerts"
    private const val CHANNEL_DESC = "Instant notifications and smart actions for M-Pesa transactions"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, parsed: MpesaTransaction, entity: TransactionEntity) {
        createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val fmt = NumberFormat.getNumberInstance(Locale.US)
        fmt.minimumFractionDigits = 0
        fmt.maximumFractionDigits = 2

        val formattedAmount = "KES ${fmt.format(parsed.amount)}"
        val counterparty = parsed.counterparty.ifBlank { "M-Pesa" }

        val title = when (parsed.direction.lowercase(Locale.ROOT)) {
            "incoming" -> "🟢 Received $formattedAmount from $counterparty"
            else -> when (parsed.category) {
                "Airtime" -> "📱 Bought $formattedAmount Airtime"
                "Withdrawal" -> "🏧 Withdrew $formattedAmount from $counterparty"
                "Fuliza" -> "💳 Fuliza: $formattedAmount"
                "M-Shwari" -> "🏦 M-Shwari: $formattedAmount"
                else -> "🔴 Paid $formattedAmount to $counterparty"
            }
        }

        val contentBuilder = StringBuilder()
        if (parsed.newBalance != null && parsed.newBalance > 0f) {
            contentBuilder.append("Balance: KES ${fmt.format(parsed.newBalance)}")
        }
        if (contentBuilder.isNotEmpty()) contentBuilder.append(" • ")
        contentBuilder.append("Category: ${parsed.category}")

        if (parsed.fulizaOutstanding > 0f) {
            contentBuilder.append(" • Fuliza Due: KES ${fmt.format(parsed.fulizaOutstanding)}")
        }

        val contentText = contentBuilder.toString()

        val notificationId = (parsed.transactionCode.hashCode() and 0x7FFFFFFF)
            .takeIf { it != 0 } ?: (System.currentTimeMillis().toInt() and 0x7FFFFFFF)

        // Main Tap Intent: opens MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "TRANSACTIONS")
            putExtra("EXTRA_TRANSACTION_ID", entity.localId)
        }
        val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(context, notificationId, openIntent, pFlags)

        // Quick Action 1: Change Category
        val changeCatIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CHANGE_CATEGORY
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SMS_ID, entity.smsId)
            putExtra(NotificationActionReceiver.EXTRA_AMOUNT, parsed.amount)
            putExtra(NotificationActionReceiver.EXTRA_COUNTERPARTY, counterparty)
            putExtra(NotificationActionReceiver.EXTRA_CATEGORY, parsed.category)
        }
        val changeCatPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            changeCatIntent,
            pFlags
        )

        // Quick Action 2: Add Note
        val addNoteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ADD_NOTE
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SMS_ID, entity.smsId)
            putExtra(NotificationActionReceiver.EXTRA_AMOUNT, parsed.amount)
            putExtra(NotificationActionReceiver.EXTRA_COUNTERPARTY, counterparty)
        }
        val addNotePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            addNoteIntent,
            pFlags
        )

        // Quick Action 3: Split Bill
        val splitBillIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SPLIT_BILL
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_AMOUNT, parsed.amount)
            putExtra(NotificationActionReceiver.EXTRA_COUNTERPARTY, counterparty)
        }
        val splitBillPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 3,
            splitBillIntent,
            pFlags
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_logo)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$contentText\nRef: ${parsed.transactionCode.ifBlank { "N/A" }} | Fee: KES ${fmt.format(parsed.fee)}"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_category, "Category", changeCatPendingIntent)
            .addAction(R.drawable.ic_info, "Note", addNotePendingIntent)
            .addAction(R.drawable.ic_balance, "Split", splitBillPendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }
}
