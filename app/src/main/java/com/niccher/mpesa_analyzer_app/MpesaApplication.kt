package com.niccher.mpesa_analyzer_app

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.niccher.mpesa_analyzer_app.helpers.SyncScheduler

class MpesaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply Material You dynamic colors to all activities
        DynamicColors.applyToActivitiesIfAvailable(this)

        SyncScheduler.updateSyncSchedule(this)
    }
}
