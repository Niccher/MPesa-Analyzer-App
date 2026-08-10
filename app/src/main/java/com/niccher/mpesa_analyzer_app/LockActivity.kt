package com.niccher.mpesa_analyzer_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.helpers.BiometricHelper

class LockActivity : AppCompatActivity() {

    private lateinit var biometricHelper: BiometricHelper

    companion object {
        var isUnlocked: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        biometricHelper = BiometricHelper(this)

        onBackPressedDispatcher.addCallback(this) {}

        val btnUnlock = findViewById<Button>(R.id.btn_unlock)
        val pinLayout = findViewById<LinearLayout>(R.id.pin_layout)
        val etPin = findViewById<EditText>(R.id.et_pin)
        val btnPinUnlock = findViewById<Button>(R.id.btn_pin_unlock)
        val tvSwitch = findViewById<TextView>(R.id.tv_switch_auth)

        val hasPin = AppPrefs.isPinEnabled(this) && AppPrefs.getPinCode(this).isNotEmpty()
        val hasBio = AppPrefs.isBiometricEnabled(this)

        // If neither is enabled, skip lock entirely
        if (!hasPin && !hasBio) {
            isUnlocked = true
            finish()
            return
        }

        if (hasPin) pinLayout.visibility = android.view.View.VISIBLE

        if (hasBio) {
            btnUnlock.visibility = android.view.View.VISIBLE
            tvSwitch.text = if (hasPin) "Use PIN instead" else ""
            showBiometricPrompt()
        }

        btnUnlock.setOnClickListener { showBiometricPrompt() }

        btnPinUnlock.setOnClickListener {
            val entered = etPin.text.toString()
            if (entered == AppPrefs.getPinCode(this)) {
                isUnlocked = true
                finish()
            } else {
                Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show()
                etPin.text.clear()
            }
        }

        tvSwitch.setOnClickListener {
            if (pinLayout.visibility == android.view.View.VISIBLE) {
                pinLayout.visibility = android.view.View.GONE
                btnUnlock.visibility = android.view.View.VISIBLE
                tvSwitch.text = if (hasPin) "Use PIN instead" else ""
            } else {
                pinLayout.visibility = android.view.View.VISIBLE
                btnUnlock.visibility = android.view.View.GONE
                tvSwitch.text = if (hasBio) "Use biometric instead" else ""
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isUnlocked && !biometricHelper.isShowing()) {
            val hasBio = AppPrefs.isBiometricEnabled(this)
            if (hasBio) showBiometricPrompt()
        }
    }

    private fun showBiometricPrompt() {
        biometricHelper.showBiometricPrompt(
            onSuccess = {
                isUnlocked = true
                finish()
            },
            onError = { error ->
                if (!error.contains("cancel", true) && !error.contains("user cancel", true)) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            },
            onFail = { }
        )
    }
}
