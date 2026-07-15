package com.niccher.mpesa_analyzer_app.helpers

import android.content.Context
import android.os.Build
import android.util.Log
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.interfaces.JsonFonePrint
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_Fone_Id
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Registers this handset with the backend (POST /process/device) and
 * stores the returned print_id for subsequent uploads.
 *
 * Called after successful token / QR login so every linked device has a fingerprint.
 */
object DeviceFingerprint {

    private const val TAG = Konstants.TAGGED

    fun buildFields(): Map<String, String> {
        return mapOf(
            "device_Device" to (Build.DEVICE ?: "unknown"),
            "device_Product" to (Build.PRODUCT ?: "unknown"),
            "device_Bootloader" to (Build.BOOTLOADER ?: "unknown"),
            "device_Type" to (Build.TYPE ?: "unknown"),
            "device_Tags" to (Build.TAGS ?: "unknown"),
            "device_Host" to (Build.HOST ?: "unknown"),
            "device_Display" to (Build.DISPLAY ?: "unknown"),
            "device_Hardware" to (Build.HARDWARE ?: "unknown"),
            "device_Fingerprint" to (Build.FINGERPRINT ?: "unknown"),
            "device_Manufacturer" to (Build.MANUFACTURER ?: "unknown"),
            "device_Brand" to (Build.BRAND ?: "unknown"),
            "device_Board" to (Build.BOARD ?: "unknown"),
            "device_User" to (Build.USER ?: "unknown"),
            "device_Model" to (Build.MODEL ?: "unknown"),
            "device_Time" to Build.TIME.toString(),
            "device_Serial" to "unknown"
        )
    }

    fun getStoredPrintId(context: Context): String {
        val prefs = context.getSharedPreferences(Konstants.SHARED_DEVICE_ID, Context.MODE_PRIVATE)
        return prefs.getString("print_id", null) ?: "nullable"
    }

    fun isRegistered(context: Context): Boolean {
        val id = getStoredPrintId(context)
        return id.isNotBlank() && id != "nullable" && id.length >= 8
    }

    fun savePrintId(context: Context, printId: String) {
        context.getSharedPreferences(Konstants.SHARED_DEVICE_ID, Context.MODE_PRIVATE)
            .edit()
            .putString("print_id", printId)
            .apply()
        Log.i(TAG, "Device fingerprint saved: $printId")
    }

/**
 * Register device fingerprint with the server.
 * @param onComplete called on main thread with (success, message)
 */
fun register(context: Context, onComplete: ((Boolean, String) -> Unit)? = null) {
    val appContext = context.applicationContext
    val backendUrl = AppPrefs.getBackendUrl(appContext)
    if (backendUrl.isBlank()) {
        Log.e(TAG, "Device register skipped: backend URL empty")
        onComplete?.invoke(false, "Backend URL not configured")
        return
    }

    val fields = buildFields()
    Log.i(TAG, "Registering device fingerprint: model=${fields["device_Model"]} brand=${fields["device_Brand"]} fingerprint=${fields["device_Fingerprint"]}")

    val service = ServiceGenerators.createService(JsonFonePrint::class.java, appContext)
    service.createPrint(fields).enqueue(object : Callback<Mod_Fone_Id> {
        override fun onResponse(call: Call<Mod_Fone_Id>, response: Response<Mod_Fone_Id>) {
            Log.d(TAG, "Device register response: code=${response.code()} successful=${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val printId = body.print_id
                Log.d(TAG, "Device register body: status=${body.status} print_id=$printId message=${body.message}")
                if (body.status == 1 && !printId.isNullOrBlank()) {
                    savePrintId(appContext, printId)
                    Log.i(TAG, "Device fingerprint saved successfully: $printId")
                    onComplete?.invoke(true, body.message ?: "Device registered")
                } else {
                    val msg = body.message ?: "Device registration failed (status=${body.status})"
                    Log.e(TAG, "Device register failed: $msg")
                    onComplete?.invoke(false, msg)
                }
            } else {
                val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Log.e(TAG, "Device register HTTP error: $err")
                onComplete?.invoke(false, "Device register failed: HTTP ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Mod_Fone_Id>, t: Throwable) {
            Log.e(TAG, "Device register network error: ${t.message}", t)
            onComplete?.invoke(false, "Device register network error: ${t.message}")
        }
    })
}
}
