package com.niccher.mpesa_analyzer_app.fragments.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.interfaces.JsonAuthUser
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_Delete_Account
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

        // Load current preferences
        switchDarkTheme.isChecked = AppPrefs.isDarkThemeEnabled(requireContext())
        switchBiometric.isChecked = AppPrefs.isBiometricEnabled(requireContext())

        // Dark Theme toggle
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setDarkThemeEnabled(requireContext(), isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Biometric Lock toggle
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setBiometricEnabled(requireContext(), isChecked)
        }

        val etBackendUrl = view.findViewById<android.widget.EditText>(R.id.et_settings_backend_url)
        val btnSaveUrl = view.findViewById<android.widget.Button>(R.id.btn_settings_save_url)

        val currentUrl = AppPrefs.getBackendUrl(requireContext())
        if (currentUrl.isNotEmpty()) {
            etBackendUrl.setText(currentUrl)
        }

        btnSaveUrl.setOnClickListener {
            val url = etBackendUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                AppPrefs.setBackendUrl(requireContext(), url)
                android.widget.Toast.makeText(requireContext(), "Backend URL saved. Please restart the app for changes to take effect.", android.widget.Toast.LENGTH_LONG).show()
            } else {
                android.widget.Toast.makeText(requireContext(), "URL cannot be empty", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        val llLogout = view.findViewById<LinearLayout>(R.id.ll_logout)
        llLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out? This will disconnect your device from the account.")
                .setPositiveButton("Logout") { _, _ ->
                    val sharedPrefs = requireContext().getSharedPreferences(Konstants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
                    sharedPrefs.edit().clear().apply()
                    android.widget.Toast.makeText(requireContext(), "Logged out successfully", android.widget.Toast.LENGTH_LONG).show()
                    val intent = Intent(requireContext(), TokenAuthActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    activity?.finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val llDeleteData = view.findViewById<LinearLayout>(R.id.ll_delete_data)
        llDeleteData.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete My Data")
                .setMessage("Are you sure you want to delete all uploaded data? Your account and device pairing will remain intact.")
                .setPositiveButton("Delete Data") { _, _ ->
                    performDataDeletion()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val llDeleteAccount = view.findViewById<LinearLayout>(R.id.ll_delete_account)
        llDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account, device pairing, and all uploaded data? This action cannot be undone.")
                .setPositiveButton("Delete Account") { _, _ ->
                    performAccountDeletion()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performDataDeletion() {
        val sharedPrefs = requireContext().getSharedPreferences(Konstants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("token", "") ?: ""
        
        if (token.isEmpty()) return

        val authService = ServiceGenerators.createService(JsonAuthUser::class.java, requireContext())
        authService.delete_data(token).enqueue(object : Callback<Mod_Delete_Account> {
            override fun onResponse(call: Call<Mod_Delete_Account>, response: Response<Mod_Delete_Account>) {
                if (response.isSuccessful && response.body()?.status == "1") {
                    android.widget.Toast.makeText(requireContext(), "Data deleted successfully", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Failed to delete data: ${response.body()?.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Mod_Delete_Account>, t: Throwable) {
                android.widget.Toast.makeText(requireContext(), "Network error", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun performAccountDeletion() {
        val sharedPrefs = requireContext().getSharedPreferences(Konstants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("token", "") ?: ""
        
        if (token.isEmpty()) return

        val authService = ServiceGenerators.createService(JsonAuthUser::class.java, requireContext())
        authService.delete_account(token).enqueue(object : Callback<Mod_Delete_Account> {
            override fun onResponse(call: Call<Mod_Delete_Account>, response: Response<Mod_Delete_Account>) {
                if (response.isSuccessful && response.body()?.status == "1") {
                    // Clear session
                    sharedPrefs.edit().clear().apply()
                    android.widget.Toast.makeText(requireContext(), "Account deleted successfully", android.widget.Toast.LENGTH_LONG).show()
                    
                    // Redirect to Auth
                    val intent = Intent(requireContext(), TokenAuthActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Failed to delete account: ${response.body()?.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Mod_Delete_Account>, t: Throwable) {
                android.widget.Toast.makeText(requireContext(), "Network error", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }
}
