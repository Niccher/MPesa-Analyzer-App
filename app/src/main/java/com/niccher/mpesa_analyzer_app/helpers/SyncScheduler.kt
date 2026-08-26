package com.niccher.mpesa_analyzer_app.helpers

import android.content.Context
import android.provider.Telephony
import android.util.Log
import androidx.work.*
import com.niccher.mpesa_analyzer_app.workers.MpesaSyncWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SyncScheduler {

    const val WORK_NIGHTLY = "NightlyMpesaSync"
    const val WORK_INTERVAL = "IntervalMpesaSync"
    private const val TAG = "SyncScheduler"

    fun updateSyncSchedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val mode = AppPrefs.getSyncMode(context)

        // Cancel both first to prevent duplicate or stale schedules
        workManager.cancelUniqueWork(WORK_NIGHTLY)
        workManager.cancelUniqueWork(WORK_INTERVAL)

        Log.d(TAG, "Updating sync schedule for mode: $mode")
        when (mode) {
            "nightly" -> scheduleNightly(context, workManager)
            "time" -> scheduleInterval(context, workManager)
        }
    }

    private fun scheduleNightly(context: Context, workManager: WorkManager) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelayMillis = target.timeInMillis - now.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<MpesaSyncWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NIGHTLY,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
        Log.d(TAG, "Scheduled nightly sync at 8:00 PM (delay ${initialDelayMillis / 1000 / 60} mins)")
    }

    private fun scheduleInterval(context: Context, workManager: WorkManager) {
        val intervalMinutes = AppPrefs.getSyncInterval(context)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<MpesaSyncWorker>(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_INTERVAL,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
        Log.d(TAG, "Scheduled interval sync every $intervalMinutes minutes")
    }

    fun getUnsyncedSmsCount(context: Context, watermark: Long): Int {
        val selection = "${Telephony.Sms._ID} > ? AND ${Telephony.Sms.TYPE} = ?"
        val selectionArgs = arrayOf(watermark.toString(), Telephony.Sms.MESSAGE_TYPE_INBOX.toString())
        val cr = context.contentResolver
        return try {
            cr.query(Telephony.Sms.CONTENT_URI, arrayOf(Telephony.Sms._ID), selection, selectionArgs, null)?.use { cursor ->
                cursor.count
            } ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error querying unsynced SMS count", e)
            0
        }
    }
}
