package com.niccher.mpesa_analyzer_app.helpers

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {

    private const val PREFS_NAME = "mpesa_analyser_prefs"
    const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"
    const val KEY_PIN_ENABLED = "pin_enabled"
    const val KEY_PIN_CODE = "pin_code"
    const val KEY_CURRENCY = "pref_currency"
    const val KEY_DATE_FORMAT = "pref_date_format"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isBiometricEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, true)

    fun setBiometricEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()

    fun isDarkThemeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DARK_THEME_ENABLED, false)

    fun setDarkThemeEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_DARK_THEME_ENABLED, enabled).apply()

    const val KEY_BACKEND_URL = "backend_url"

    fun getBackendUrl(context: Context): String =
        prefs(context).getString(KEY_BACKEND_URL, "") ?: ""

    fun setBackendUrl(context: Context, url: String) =
        prefs(context).edit().putString(KEY_BACKEND_URL, url).apply()

    // PIN Lock
    fun isPinEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PIN_ENABLED, false)

    fun setPinEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()

    fun getPinCode(context: Context): String =
        prefs(context).getString(KEY_PIN_CODE, "") ?: ""

    fun setPinCode(context: Context, pin: String) =
        prefs(context).edit().putString(KEY_PIN_CODE, pin).apply()

    // Cached server preferences
    fun getCurrency(context: Context): String =
        prefs(context).getString(KEY_CURRENCY, "KES") ?: "KES"

    fun setCurrency(context: Context, currency: String) =
        prefs(context).edit().putString(KEY_CURRENCY, currency).apply()

    fun getDateFormat(context: Context): String =
        prefs(context).getString(KEY_DATE_FORMAT, "Y-m-d") ?: "Y-m-d"

    fun setDateFormat(context: Context, format: String) =
        prefs(context).edit().putString(KEY_DATE_FORMAT, format).apply()
}
