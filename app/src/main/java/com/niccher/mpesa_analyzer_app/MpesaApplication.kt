package com.niccher.mpesa_analyzer_app

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import com.niccher.mpesa_analyzer_app.workers.MpesaSyncWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MpesaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply Material You dynamic colors to all activities
        DynamicColors.applyToActivitiesIfAvailable(this)

        scheduleNightlySync()
    }

    private fun scheduleNightlySync() {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If it's already past 8:00 PM today, target 8:00 PM tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelayMillis = target.timeInMillis - now.timeInMillis

        // Only run when there is internet connection
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<MpesaSyncWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NightlyMpesaSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
