package com.niccher.my_mpesa_analyzer

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.niccher.my_mpesa_analyzer.helpers.AppPrefs
import com.niccher.my_mpesa_analyzer.helpers.BiometricHelper

class LockActivity : AppCompatActivity() {

    private lateinit var biometricHelper: BiometricHelper

    companion object {
        var isUnlocked: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If biometric is disabled, skip lock entirely
        if (!AppPrefs.isBiometricEnabled(this)) {
            isUnlocked = true
            finish()
            return
        }
        setContentView(R.layout.activity_lock)

        // Prevent screenshots/peeking in recent apps
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        biometricHelper = BiometricHelper(this)

        // Trap: back button keeps the user on the lock screen
        // They cannot go "back" to the main app without authenticating
        onBackPressedDispatcher.addCallback(this) {
            // Do nothing — back is disabled while on the lock screen
        }

        findViewById<Button>(R.id.btn_unlock).setOnClickListener {
            showBiometricPrompt()
        }

        // Show prompt immediately on creation
        showBiometricPrompt()
    }

    override fun onResume() {
        super.onResume()
        // Show the prompt again if returned to this screen without being unlocked
        if (!isUnlocked && !biometricHelper.isShowing()) {
            showBiometricPrompt()
        }
    }

    private fun showBiometricPrompt() {
        biometricHelper.showBiometricPrompt(
            onSuccess = {
                isUnlocked = true
                finish()
            },
            onError = { error ->
                // Do NOT show cancellation errors to user — just let them retry via button
                if (!error.contains("cancel", true) && !error.contains("user cancel", true)) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
                // Stay on lock screen — back is disabled so user MUST authenticate
            },
            onFail = {
                // A single failed attempt (e.g. wrong fingerprint) is not fatal
                // The system biometric dialog handles retry UI automatically
            }
        )
    }
}
