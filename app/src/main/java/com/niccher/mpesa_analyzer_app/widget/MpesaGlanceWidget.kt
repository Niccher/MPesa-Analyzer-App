package com.niccher.mpesa_analyzer_app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.niccher.mpesa_analyzer_app.MainActivity
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.database.AppDatabase
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.helpers.MpesaParser
import com.niccher.mpesa_analyzer_app.services.UploadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class MpesaGlanceWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_SYNC_WIDGET = "com.niccher.mpesa_analyzer_app.ACTION_SYNC_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MpesaGlanceWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isNotEmpty()) {
                val intent = Intent(context, MpesaGlanceWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == ACTION_SYNC_WIDGET) {
            val uploadIntent = Intent(context, UploadService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(uploadIntent)
                } else {
                    context.startService(uploadIntent)
                }
            } catch (e: Exception) {
                android.util.Log.e("MpesaGlanceWidget", "Failed to start UploadService from widget", e)
            }
        } else if (action == UploadService.ACTION_UPLOAD_COMPLETE ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == Intent.ACTION_TIME_TICK
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MpesaGlanceWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_mpesa_glance)

        val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Tap root: open MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, pFlags)
        views.setOnClickPendingIntent(R.id.widget_root, openPendingIntent)

        // Tap sync: trigger UploadService
        val syncIntent = Intent(context, MpesaGlanceWidget::class.java).apply {
            action = ACTION_SYNC_WIDGET
        }
        val syncPendingIntent = PendingIntent.getBroadcast(context, 1, syncIntent, pFlags)
        views.setOnClickPendingIntent(R.id.widget_btn_sync, syncPendingIntent)

        // Load data asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val fmt = NumberFormat.getNumberInstance(Locale.US)
            fmt.minimumFractionDigits = 0
            fmt.maximumFractionDigits = 0

            val db = AppDatabase.getDatabase(context)
            val recentList = try {
                db.transactionDao().getRecentTransactions(30)
            } catch (e: Exception) {
                emptyList()
            }

            var latestBalance: Float? = null
            for (tx in recentList) {
                val parsed = MpesaParser.parseMessage(tx.body)
                if (parsed?.newBalance != null && parsed.newBalance > 0f) {
                    latestBalance = parsed.newBalance
                    break
                }
            }

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = cal.timeInMillis

            var todaySpend = 0f
            var monthSpend = 0f
            for (tx in recentList) {
                if (tx.direction == "outgoing") {
                    if (tx.timestamp >= startOfDay) todaySpend += tx.amount
                    if (tx.timestamp >= startOfMonth) monthSpend += tx.amount
                }
            }

            val safeToday = AppPrefs.getSafeToSpendToday(context, monthSpend)

            val balanceText = if (latestBalance != null) "KES ${fmt.format(latestBalance)}" else "KES --"
            val spendText = "Today: KES ${fmt.format(todaySpend)} • Safe: KES ${fmt.format(safeToday)}"
            val badgeText = if (todaySpend > safeToday && safeToday > 0f) "Over Budget" else "On Track"

            views.setTextViewText(R.id.widget_tv_balance, balanceText)
            views.setTextViewText(R.id.widget_tv_spend, spendText)
            views.setTextViewText(R.id.widget_tv_badge, badgeText)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
