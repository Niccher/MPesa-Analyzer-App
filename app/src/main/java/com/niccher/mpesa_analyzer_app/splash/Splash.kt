package com.niccher.mpesa_analyzer_app.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.niccher.mpesa_analyzer_app.MainActivity
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Smooth 800ms brand display followed by fast transition
        Handler(Looper.getMainLooper()).postDelayed({
            proceedToNextScreen()
        }, 800)
    }

    private fun proceedToNextScreen() {
        val currentUrl = AppPrefs.getBackendUrl(this)
        val isValid = checkValidity() == "1"

        val targetActivity = if (isValid && currentUrl.isNotEmpty()) {
            MainActivity::class.java
        } else {
            TokenAuthActivity::class.java
        }

        val intent = Intent(this, targetActivity).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun checkValidity(): String {
        val sharedPreferences = getSharedPreferences(Constants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        return sharedPreferences.getString("status", "3") ?: "3"
    }
}