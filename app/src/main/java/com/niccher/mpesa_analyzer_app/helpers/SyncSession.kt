package com.niccher.mpesa_analyzer_app.helpers

import android.content.Context
import java.util.UUID

object SyncSession {
    private const val PREFS = "sync_session"
    private const val KEY_ATTEMPT = "attempt_count"

    var sessionId: String? = null
        private set

    var attemptNumber: Int = 0
        private set

    @Synchronized
    fun begin(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        attemptNumber = prefs.getInt(KEY_ATTEMPT, 0) + 1
        prefs.edit().putInt(KEY_ATTEMPT, attemptNumber).apply()
        val newSession = UUID.randomUUID().toString()
        sessionId = newSession
        return newSession
    }

    fun end() {
        sessionId = null
    }
}
