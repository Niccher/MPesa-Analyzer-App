package com.niccher.mpesa_analyzer_app.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.niccher.mpesa_analyzer_app.MainActivity

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CHANGE_CATEGORY = "com.niccher.mpesa_analyzer_app.ACTION_CHANGE_CATEGORY"
        const val ACTION_ADD_NOTE = "com.niccher.mpesa_analyzer_app.ACTION_ADD_NOTE"
        const val ACTION_SPLIT_BILL = "com.niccher.mpesa_analyzer_app.ACTION_SPLIT_BILL"

        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_SMS_ID = "extra_sms_id"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_COUNTERPARTY = "extra_counterparty"
        const val EXTRA_CATEGORY = "extra_category"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val smsId = intent.getLongExtra(EXTRA_SMS_ID, -1L)
        val amount = intent.getFloatExtra(EXTRA_AMOUNT, 0f)
        val counterparty = intent.getStringExtra(EXTRA_COUNTERPARTY) ?: ""
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""

        if (notifId != -1) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(notifId)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NOTIFICATION_ACTION", action)
            putExtra(EXTRA_SMS_ID, smsId)
            putExtra(EXTRA_AMOUNT, amount)
            putExtra(EXTRA_COUNTERPARTY, counterparty)
            putExtra(EXTRA_CATEGORY, category)
        }

        when (action) {
            ACTION_CHANGE_CATEGORY -> {
                Toast.makeText(context, "Change Category for $counterparty", Toast.LENGTH_SHORT).show()
                context.startActivity(mainIntent)
            }
            ACTION_ADD_NOTE -> {
                Toast.makeText(context, "Add Note for KES $amount", Toast.LENGTH_SHORT).show()
                context.startActivity(mainIntent)
            }
            ACTION_SPLIT_BILL -> {
                Toast.makeText(context, "Split Bill: KES $amount", Toast.LENGTH_SHORT).show()
                context.startActivity(mainIntent)
            }
        }
    }
}
