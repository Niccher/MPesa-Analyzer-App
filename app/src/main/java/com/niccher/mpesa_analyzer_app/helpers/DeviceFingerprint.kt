package com.niccher.mpesa_analyzer_app.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.interfaces.JsonFonePrint
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_Fone_Id
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.util.Locale
import java.util.TimeZone

/**
 * Registers this handset with the backend (POST /process/device) and
 * stores the returned print_id for subsequent uploads.
 *
 * Called after successful token / QR login so every linked device has a fingerprint.
 *
 * The fingerprint is a SHA-256 composite of permission-free signals (ANDROID_ID,
 * app signing certificate, Build.FINGERPRINT, screen resolution) so that two
 * handsets of the same model do not collapse into the same print_id.
 */
object DeviceFingerprint {

    private const val TAG = Konstants.TAGGED

    fun buildFields(context: Context): Map<String, String> {
        val androidId = getAndroidId(context)
        val certHash = appCertHash(context)
        val screen = screenMetrics(context)
        val composite = sha256Hex(
            "$androidId|$certHash|${Build.FINGERPRINT ?: "unknown"}|${screen["device_ScreenWidth"]}x${screen["device_ScreenHeight"]}"
        )

        return mapOf(
            "device_Device" to (Build.DEVICE ?: "unknown"),
            "device_Product" to (Build.PRODUCT ?: "unknown"),
            "device_Bootloader" to (Build.BOOTLOADER ?: "unknown"),
            "device_Type" to (Build.TYPE ?: "unknown"),
            "device_Tags" to (Build.TAGS ?: "unknown"),
            "device_Host" to (Build.HOST ?: "unknown"),
            "device_Display" to (Build.DISPLAY ?: "unknown"),
            "device_Hardware" to (Build.HARDWARE ?: "unknown"),
            "device_Fingerprint" to composite,
            "device_Manufacturer" to (Build.MANUFACTURER ?: "unknown"),
            "device_Brand" to (Build.BRAND ?: "unknown"),
            "device_Board" to (Build.BOARD ?: "unknown"),
            "device_User" to (Build.USER ?: "unknown"),
            "device_Model" to (Build.MODEL ?: "unknown"),
            "device_Time" to Build.TIME.toString(),
            "device_Serial" to "unknown",
            "device_AndroidId" to androidId,
            "device_AppCertHash" to certHash,
            "device_AppVersion" to appVersion(context),
            "device_FirstInstallTime" to installTime(context, first = true),
            "device_LastUpdateTime" to installTime(context, first = false),
            "device_Sensors" to sensorHash(context),
            "device_ScreenWidth" to (screen["device_ScreenWidth"] ?: "0"),
            "device_ScreenHeight" to (screen["device_ScreenHeight"] ?: "0"),
            "device_DensityDpi" to (screen["device_DensityDpi"] ?: "0"),
            "device_Xdpi" to (screen["device_Xdpi"] ?: "0"),
            "device_Ydpi" to (screen["device_Ydpi"] ?: "0"),
            "device_Locale" to Locale.getDefault().toLanguageTag(),
            "device_Timezone" to TimeZone.getDefault().id,
            "device_CpuCount" to Runtime.getRuntime().availableProcessors().toString(),
            "device_Abis" to (Build.SUPPORTED_ABIS.joinToString(",") { it }).ifEmpty { "unknown" },
            "device_StorageTotal" to storageBytes(context, available = false).toString(),
            "device_StorageAvailable" to storageBytes(context, available = true).toString(),
            "device_BatteryCapacity" to batteryCapacity(context).toString()
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

    val fields = buildFields(appContext)
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

    private fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun appCertHash(context: Context): String {
        return try {
            val pm = context.packageManager
            val cert = try {
                val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                info.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
            } catch (e: Exception) {
                null
            } ?: pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                .signatures?.firstOrNull()?.toByteArray()
            if (cert != null) sha256Hex(cert) else "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun appVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun installTime(context: Context, first: Boolean): String {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            (if (first) info.firstInstallTime else info.lastUpdateTime).toString()
        } catch (e: Exception) {
            "0"
        }
    }

    private fun sensorHash(context: Context): String {
        return try {
            val sensors = context.getSystemService(Context.SENSOR_SERVICE)
                ?.let { it as android.hardware.SensorManager }
                ?.getSensorList(android.hardware.Sensor.TYPE_ALL)
            if (sensors.isNullOrEmpty()) {
                "none"
            } else {
                sha256Hex(
                    sensors.joinToString(",") { s -> "${s.type}:${s.vendor}:${s.name}" }
                )
            }
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun screenMetrics(context: Context): Map<String, String> {
        return try {
            val dm: DisplayMetrics = context.resources.displayMetrics
            val w = maxOf(dm.widthPixels, dm.heightPixels)
            val h = minOf(dm.widthPixels, dm.heightPixels)
            mapOf(
                "device_ScreenWidth" to w.toString(),
                "device_ScreenHeight" to h.toString(),
                "device_DensityDpi" to dm.densityDpi.toString(),
                "device_Xdpi" to dm.xdpi.toString(),
                "device_Ydpi" to dm.ydpi.toString()
            )
        } catch (e: Exception) {
            mapOf(
                "device_ScreenWidth" to "0",
                "device_ScreenHeight" to "0",
                "device_DensityDpi" to "0",
                "device_Xdpi" to "0",
                "device_Ydpi" to "0"
            )
        }
    }

    private fun storageBytes(context: Context, available: Boolean): Long {
        return try {
            val stats = StatFs(Environment.getDataDirectory().path)
            if (available) stats.availableBytes else stats.totalBytes
        } catch (e: Exception) {
            0L
        }
    }

    private fun batteryCapacity(context: Context): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    private fun sha256Hex(input: String): String = sha256Hex(input.toByteArray(Charsets.UTF_8))

    private fun sha256Hex(bytes: ByteArray): String {
        return try {
            MessageDigest.getInstance("SHA-256")
                .digest(bytes)
                .joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}
