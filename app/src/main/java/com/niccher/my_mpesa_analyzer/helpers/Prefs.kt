package com.niccher.my_mpesa_analyzer.helpers

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import java.text.SimpleDateFormat

class Prefs {

    private val kon = Konstants

    private var prefSms: SharedPreferences? = null
    private var sharedEditor: SharedPreferences.Editor? = null

    fun getFileType(fileName: String, timeAt: String, cntt: Context) {

        if (fileName.startsWith("sms_All_")) {
            prefSms = cntt.getSharedPreferences(kon.SHARED_LAST_TIME, MODE_PRIVATE) // Use Konstants object
            sharedEditor = prefSms?.edit()
            sharedEditor?.putString("last_upload_name", fileName)
            sharedEditor?.putString("last_upload_time", timeAt)
            sharedEditor?.apply()
            Log.e(kon.TAGGED, "get_FileType: as SMS") // Use Konstants object
        }
    }

    fun getTimeStamp(cnt: Context): String {
        var timestamp = ""
        val prefRead = cnt.getSharedPreferences(kon.SHARED_LAST_TIME, MODE_PRIVATE)
        val value = prefRead.getString(kon.SHARED_LAST_TIME, "0000000")

        try {
            val timestampLong = value?.toLong() ?: 0L // Use safe call and elvis operator
            val sdf = SimpleDateFormat("MMM dd HH:mm:ss")
            timestamp = sdf.format(timestampLong)
        } catch (es: Exception) {
            timestamp = "Not set"
        }

        return timestamp
    }

    fun getPrefsAuth(ty: String, cntt: Context): String {
        var id = ""
        when (ty) {
            "auth" -> {
                val prefAuth = cntt.getSharedPreferences(kon.SHARED_AUTH_LOGIN, MODE_PRIVATE)
                id = prefAuth.getString("uuid", "nullable") ?: "nullable" // Use elvis operator
            }
            "print" -> {
                val prefDevId = cntt.getSharedPreferences(kon.SHARED_DEVICE_ID, MODE_PRIVATE)
                id = prefDevId.getString("print_id", "nullable") ?: "nullable" // Use elvis operator
            }
            "loot_count" -> {
                val prefLootCount = cntt.getSharedPreferences(kon.SHARED_LOOT_COUNT, MODE_PRIVATE)
                id = prefLootCount.getInt("loots", 0).toString()
            }
        }
        return id
    }
}