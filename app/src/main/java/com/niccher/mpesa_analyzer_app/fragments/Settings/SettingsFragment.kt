package com.niccher.mpesa_analyzer_app.fragments.Settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.helpers.SyncScheduler
import com.niccher.mpesa_analyzer_app.api.AuthApiService
import com.niccher.mpesa_analyzer_app.api.SettingsApiService
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.models.DeleteAccountModel
import com.niccher.mpesa_analyzer_app.models.GenericResponseModel
import com.niccher.mpesa_analyzer_app.models.ProfileResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    // Time-interval options in minutes (maps to slider positions 0-4)
    private val intervalOptions = intArrayOf(30, 45, 60, 120, 180)
    // SMS count batch options (maps to slider positions 0-6)
    private val countOptions = intArrayOf(10, 20, 30, 40, 50, 80, 100)

    private val tickerHandler = Handler(Looper.getMainLooper())
    private var tickerRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchDarkTheme = view.findViewById<SwitchMaterial>(R.id.switch_dark_theme)
        val switchBiometric = view.findViewById<SwitchMaterial>(R.id.switch_biometric)
        val switchPin = view.findViewById<SwitchMaterial>(R.id.switch_pin)

        switchDarkTheme.isChecked = AppPrefs.isDarkThemeEnabled(requireContext())
        switchBiometric.isChecked = AppPrefs.isBiometricEnabled(requireContext())
        switchPin.isChecked = AppPrefs.isPinEnabled(requireContext())

        // Dark Theme
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setDarkThemeEnabled(requireContext(), isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Biometric
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setBiometricEnabled(requireContext(), isChecked)
        }

        // PIN
        switchPin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showPinSetupDialog()
            } else {
                AppPrefs.setPinEnabled(requireContext(), false)
                AppPrefs.setPinCode(requireContext(), "")
            }
        }

        // Backend URL
        val etBackendUrl = view.findViewById<EditText>(R.id.et_settings_backend_url)
        val btnSaveUrl = view.findViewById<Button>(R.id.btn_settings_save_url)
        val currentUrl = AppPrefs.getBackendUrl(requireContext())
        if (currentUrl.isNotEmpty()) etBackendUrl.setText(currentUrl)

        btnSaveUrl.setOnClickListener {
            val url = etBackendUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                AppPrefs.setBackendUrl(requireContext(), url)
                Toast.makeText(requireContext(), "URL saved. Restart app.", Toast.LENGTH_LONG).show()
            }
        }

        // Profile
        val etProfileName = view.findViewById<EditText>(R.id.et_profile_name)
        val btnSaveProfile = view.findViewById<Button>(R.id.btn_save_profile)
        val sharedPrefs = requireContext().getSharedPreferences(Constants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        etProfileName.setText(sharedPrefs.getString("user_name", ""))

        btnSaveProfile.setOnClickListener {
            val name = etProfileName.text.toString().trim()
            if (name.isNotEmpty()) {
                val token = sharedPrefs.getString("token", "") ?: ""
                if (token.isNotEmpty()) {
                    val settingsService = ServiceGenerator.createService(SettingsApiService::class.java, requireContext())
                    settingsService.updateProfile(mapOf("varToken" to token, "username" to name)).enqueue(object : Callback<GenericResponseModel> {
                        override fun onResponse(call: Call<GenericResponseModel>, response: Response<GenericResponseModel>) {
                            if (response.isSuccessful) {
                                sharedPrefs.edit().putString("user_name", name).apply()
                                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<GenericResponseModel>, t: Throwable) {
                            Toast.makeText(requireContext(), "Net error", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    sharedPrefs.edit().putString("user_name", name).apply()
                    Toast.makeText(requireContext(), "Name saved locally", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ── Sync Mode Configuration ──────────────────────────────────────────
        setupSyncModeUi(view)

        // Manual Sync
        val btnSync = view.findViewById<Button>(R.id.btn_manual_sync)
        val tvSyncStatus = view.findViewById<TextView>(R.id.tv_sync_status)
        btnSync.setOnClickListener {
            val token = sharedPrefs.getString("token", "") ?: ""
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnSync.isEnabled = false
            btnSync.text = "Scanning..."
            val settingsService = ServiceGenerator.createService(SettingsApiService::class.java, requireContext())
            settingsService.scanTrigger(token).enqueue(object : Callback<GenericResponseModel> {
                override fun onResponse(call: Call<GenericResponseModel>, response: Response<GenericResponseModel>) {
                    btnSync.isEnabled = true
                    btnSync.text = "Trigger Scan Now"
                    if (response.isSuccessful) {
                        tvSyncStatus.text = "Scan started: ${response.body()?.message ?: "OK"}"
                        Toast.makeText(requireContext(), "Scan triggered", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponseModel>, t: Throwable) {
                    btnSync.isEnabled = true
                    btnSync.text = "Trigger Scan Now"
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Data Export
        view.findViewById<Button>(R.id.btn_export_csv).setOnClickListener {
            exportData("csv")
        }
        view.findViewById<Button>(R.id.btn_export_json).setOnClickListener {
            exportData("json")
        }


        // Logout, Delete Data, Delete Account
        view.findViewById<LinearLayout>(R.id.ll_logout).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Disconnect this device?")
                .setPositiveButton("Logout") { _, _ ->
                    sharedPrefs.edit().clear().apply()
                    val intent = Intent(requireContext(), TokenAuthActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    activity?.finish()
                }
                .setNegativeButton("Cancel", null).show()
        }

        view.findViewById<LinearLayout>(R.id.ll_delete_data).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete My Data")
                .setMessage("Delete all uploaded data? Account remains.")
                .setPositiveButton("Delete") { _, _ -> performDataDeletion(sharedPrefs) }
                .setNegativeButton("Cancel", null).show()
        }

        view.findViewById<LinearLayout>(R.id.ll_delete_account).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Permanently delete everything?")
                .setPositiveButton("Delete") { _, _ -> performAccountDeletion(sharedPrefs) }
                .setNegativeButton("Cancel", null).show()
        }
    }

    // ─── Sync Mode UI Setup ────────────────────────────────────────────────

    private fun setupSyncModeUi(view: View) {
        val ctx = requireContext()

        val rgSyncMode = view.findViewById<RadioGroup>(R.id.rg_sync_mode)
        val llTimeContainer = view.findViewById<LinearLayout>(R.id.ll_sync_time_container)
        val llCountContainer = view.findViewById<LinearLayout>(R.id.ll_sync_count_container)
        val sliderInterval = view.findViewById<Slider>(R.id.slider_sync_interval)
        val sliderCount = view.findViewById<Slider>(R.id.slider_sync_count)
        val tvIntervalLabel = view.findViewById<TextView>(R.id.tv_sync_interval_label)
        val tvCountLabel = view.findViewById<TextView>(R.id.tv_sync_count_label)
        val tvNextSyncInfo = view.findViewById<TextView>(R.id.tv_next_sync_info)

        // Restore saved values
        val savedInterval = AppPrefs.getSyncInterval(ctx)
        val savedCount = AppPrefs.getSyncCountThreshold(ctx)
        sliderInterval.value = (intervalOptions.indexOfFirst { it == savedInterval }.takeIf { it >= 0 } ?: 0).toFloat()
        sliderCount.value = (countOptions.indexOfFirst { it == savedCount }.takeIf { it >= 0 } ?: 0).toFloat()
        tvIntervalLabel.text = "Upload Interval: ${intervalOptions[sliderInterval.value.toInt()]} minutes"
        tvCountLabel.text = "Upload batch size: ${countOptions[sliderCount.value.toInt()]} SMS"

        // Restore selected mode and configure visibility
        val savedMode = AppPrefs.getSyncMode(ctx)
        when (savedMode) {
            "immediate" -> rgSyncMode.check(R.id.rb_sync_immediate)
            "time"      -> rgSyncMode.check(R.id.rb_sync_time)
            "count"     -> rgSyncMode.check(R.id.rb_sync_count)
            "nightly"   -> rgSyncMode.check(R.id.rb_sync_nightly)
        }
        updateSyncContainerVisibility(savedMode, llTimeContainer, llCountContainer)
        startCountdownTicker(ctx, savedMode, tvNextSyncInfo)

        // Slider: time interval
        sliderInterval.addOnChangeListener { _, value, _ ->
            val mins = intervalOptions[value.toInt()]
            tvIntervalLabel.text = "Upload Interval: $mins minutes"
            AppPrefs.setSyncInterval(ctx, mins)
            if (AppPrefs.getSyncMode(ctx) == "time") {
                SyncScheduler.updateSyncSchedule(ctx)
                startCountdownTicker(ctx, "time", tvNextSyncInfo)
            }
        }

        // Slider: count threshold
        sliderCount.addOnChangeListener { _, value, _ ->
            val cnt = countOptions[value.toInt()]
            tvCountLabel.text = "Upload batch size: $cnt SMS"
            AppPrefs.setSyncCountThreshold(ctx, cnt)
            if (AppPrefs.getSyncMode(ctx) == "count") {
                startCountdownTicker(ctx, "count", tvNextSyncInfo)
            }
        }

        // RadioGroup: mode selection
        rgSyncMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rb_sync_immediate -> "immediate"
                R.id.rb_sync_time      -> "time"
                R.id.rb_sync_count     -> "count"
                R.id.rb_sync_nightly   -> "nightly"
                else                   -> "immediate"
            }
            AppPrefs.setSyncMode(ctx, mode)
            SyncScheduler.updateSyncSchedule(ctx)
            updateSyncContainerVisibility(mode, llTimeContainer, llCountContainer)
            startCountdownTicker(ctx, mode, tvNextSyncInfo)
        }
    }

    private fun updateSyncContainerVisibility(
        mode: String,
        llTime: LinearLayout,
        llCount: LinearLayout
    ) {
        llTime.visibility = if (mode == "time") View.VISIBLE else View.GONE
        llCount.visibility = if (mode == "count") View.VISIBLE else View.GONE
    }

    private fun startCountdownTicker(ctx: Context, mode: String, tvInfo: TextView) {
        tickerRunnable?.let { tickerHandler.removeCallbacks(it) }
        val runnable = object : Runnable {
            override fun run() {
                if (!isAdded) return
                val infoText: String = when (mode) {
                    "immediate" -> "⚡ Uploads happen as soon as a new M-Pesa SMS is received"
                    "nightly" -> {
                        val now = Calendar.getInstance()
                        val target = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 20)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
                        }
                        val remaining = target.timeInMillis - now.timeInMillis
                        "🌙 Next nightly upload in ${formatDuration(remaining)}"
                    }
                    "time" -> {
                        val intervalMs = AppPrefs.getSyncInterval(ctx) * 60_000L
                        val lastSync = AppPrefs.getLastSyncSuccessTime(ctx)
                        val nextSync = if (lastSync == 0L) System.currentTimeMillis() + intervalMs else lastSync + intervalMs
                        val remaining = nextSync - System.currentTimeMillis()
                        if (remaining <= 0) "⏰ Uploading soon…"
                        else "⏱ Next upload in ${formatDuration(remaining)}"
                    }
                    "count" -> {
                        val threshold = AppPrefs.getSyncCountThreshold(ctx)
                        val watermark = ctx.getSharedPreferences(Constants.SHARED_LAST_TIME, Context.MODE_PRIVATE)
                            .getLong(Constants.SHARED_LAST_SMS_ID, 0L)
                        val current = SyncScheduler.getUnsyncedSmsCount(ctx, watermark)
                        "📩 $current / $threshold new SMS buffered — upload triggers at $threshold"
                    }
                    else -> ""
                }
                tvInfo.text = infoText
                tickerHandler.postDelayed(this, if (mode == "count") 5_000L else 1_000L)
            }
        }
        tickerRunnable = runnable
        tickerHandler.post(runnable)
    }

    private fun formatDuration(millis: Long): String {
        if (millis <= 0) return "0s"
        val h = TimeUnit.MILLISECONDS.toHours(millis)
        val m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return when {
            h > 0 -> "${h}h ${m}m ${s}s"
            m > 0 -> "${m}m ${s}s"
            else  -> "${s}s"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tickerRunnable?.let { tickerHandler.removeCallbacks(it) }
    }

    // ─── PIN Dialog ────────────────────────────────────────────────────────

    private fun showPinSetupDialog() {
        val input = EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "Enter 4-digit PIN"
        input.maxLines = 1

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set PIN")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val pin = input.text.toString()
                if (pin.length == 4 && pin.all { it.isDigit() }) {
                    AppPrefs.setPinCode(requireContext(), pin)
                    AppPrefs.setPinEnabled(requireContext(), true)
                    Toast.makeText(requireContext(), "PIN set", Toast.LENGTH_SHORT).show()
                } else {
                    AppPrefs.setPinEnabled(requireContext(), false)
                    Toast.makeText(requireContext(), "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                AppPrefs.setPinEnabled(requireContext(), false)
            }
            .show()
    }

    private fun exportData(format: String) {
        val sharedPrefs = requireContext().getSharedPreferences(Constants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val baseUrl = AppPrefs.getBackendUrl(requireContext())
        val url = "${baseUrl}/api/export/$format?varToken=$token"
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    }

    private fun performDataDeletion(sharedPrefs: android.content.SharedPreferences) {
        val token = sharedPrefs.getString("token", "") ?: return
        val authService = ServiceGenerator.createService(AuthApiService::class.java, requireContext())
        authService.deleteData(token).enqueue(object : Callback<DeleteAccountModel> {
            override fun onResponse(call: Call<DeleteAccountModel>, response: Response<DeleteAccountModel>) {
                Toast.makeText(requireContext(), if (response.isSuccessful) "Data deleted" else "Failed", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<DeleteAccountModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun performAccountDeletion(sharedPrefs: android.content.SharedPreferences) {
        val token = sharedPrefs.getString("token", "") ?: return
        val authService = ServiceGenerator.createService(AuthApiService::class.java, requireContext())
        authService.deleteAccount(token).enqueue(object : Callback<DeleteAccountModel> {
            override fun onResponse(call: Call<DeleteAccountModel>, response: Response<DeleteAccountModel>) {
                if (response.isSuccessful) {
                    sharedPrefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_LONG).show()
                    val intent = Intent(requireContext(), TokenAuthActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    activity?.finish()
                }
            }
            override fun onFailure(call: Call<DeleteAccountModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
