package com.niccher.mpesa_analyzer_app.fragments.Settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.interfaces.JsonAuthUser
import com.niccher.mpesa_analyzer_app.interfaces.JsonSettings
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_Delete_Account
import com.niccher.mpesa_analyzer_app.models.Mod_Generic_Response
import com.niccher.mpesa_analyzer_app.models.Mod_Profile_Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Frag_Settings : Fragment() {

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
        val sharedPrefs = requireContext().getSharedPreferences(Konstants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        etProfileName.setText(sharedPrefs.getString("user_name", ""))

        btnSaveProfile.setOnClickListener {
            val name = etProfileName.text.toString().trim()
            if (name.isNotEmpty()) {
                val token = sharedPrefs.getString("token", "") ?: ""
                if (token.isNotEmpty()) {
                    val settingsService = ServiceGenerators.createService(JsonSettings::class.java, requireContext())
                    settingsService.updateProfile(mapOf("varToken" to token, "username" to name)).enqueue(object : Callback<Mod_Generic_Response> {
                        override fun onResponse(call: Call<Mod_Generic_Response>, response: Response<Mod_Generic_Response>) {
                            if (response.isSuccessful) {
                                sharedPrefs.edit().putString("user_name", name).apply()
                                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<Mod_Generic_Response>, t: Throwable) {
                            Toast.makeText(requireContext(), "Net error", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    sharedPrefs.edit().putString("user_name", name).apply()
                    Toast.makeText(requireContext(), "Name saved locally", Toast.LENGTH_SHORT).show()
                }
            }
        }

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
            val settingsService = ServiceGenerators.createService(JsonSettings::class.java, requireContext())
            settingsService.scanTrigger(token).enqueue(object : Callback<Mod_Generic_Response> {
                override fun onResponse(call: Call<Mod_Generic_Response>, response: Response<Mod_Generic_Response>) {
                    btnSync.isEnabled = true
                    btnSync.text = "Trigger Scan Now"
                    if (response.isSuccessful) {
                        tvSyncStatus.text = "Scan started: ${response.body()?.message ?: "OK"}"
                        Toast.makeText(requireContext(), "Scan triggered", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Mod_Generic_Response>, t: Throwable) {
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
        val sharedPrefs = requireContext().getSharedPreferences(Konstants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
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
        val authService = ServiceGenerators.createService(JsonAuthUser::class.java, requireContext())
        authService.delete_data(token).enqueue(object : Callback<Mod_Delete_Account> {
            override fun onResponse(call: Call<Mod_Delete_Account>, response: Response<Mod_Delete_Account>) {
                Toast.makeText(requireContext(), if (response.isSuccessful) "Data deleted" else "Failed", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Mod_Delete_Account>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun performAccountDeletion(sharedPrefs: android.content.SharedPreferences) {
        val token = sharedPrefs.getString("token", "") ?: return
        val authService = ServiceGenerators.createService(JsonAuthUser::class.java, requireContext())
        authService.delete_account(token).enqueue(object : Callback<Mod_Delete_Account> {
            override fun onResponse(call: Call<Mod_Delete_Account>, response: Response<Mod_Delete_Account>) {
                if (response.isSuccessful) {
                    sharedPrefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_LONG).show()
                    val intent = Intent(requireContext(), TokenAuthActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    activity?.finish()
                }
            }
            override fun onFailure(call: Call<Mod_Delete_Account>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
