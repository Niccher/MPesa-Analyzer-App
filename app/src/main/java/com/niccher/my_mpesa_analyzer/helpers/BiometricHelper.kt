package com.niccher.my_mpesa_analyzer.helpers

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class BiometricHelper(private val activity: AppCompatActivity) {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var isPromptShowing = false

    fun isShowing(): Boolean = isPromptShowing

    fun showBiometricPrompt(
        title: String = "App Lock",
        subtitle: String = "Authenticate to unlock MyMpesaAnalyser",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFail: () -> Unit
    ) {
        if (isPromptShowing) return

        val biometricManager = BiometricManager.from(activity)
        
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG
        }

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Device can use biometric or device credential
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                onError("No biometric hardware detected")
                return
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onError("Biometric hardware is currently unavailable")
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // If no biometric is enrolled, we might want to prompt user to set it up
                // or just fallback to device credential if allowed.
                // For now, let's treat it as an error to setup.
                onError("No biometric data enrolled on this device")
                return
            }
            else -> {
                onError("Biometric authentication is not available")
                return
            }
        }

        executor = ContextCompat.getMainExecutor(activity)
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isPromptShowing = false
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isPromptShowing = false
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Note: onAuthenticationFailed doesn't dismiss the dialog
                    onFail()
                }
            })

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)

        // Negative button is only allowed if DEVICE_CREDENTIAL is NOT set
        if ((authenticators and DEVICE_CREDENTIAL) == 0) {
            builder.setNegativeButtonText("Cancel")
        }

        promptInfo = builder.build()
        isPromptShowing = true
        biometricPrompt.authenticate(promptInfo)
    }
}
