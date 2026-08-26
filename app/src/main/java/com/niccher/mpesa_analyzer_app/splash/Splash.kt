package com.niccher.mpesa_analyzer_app.splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.niccher.mpesa_analyzer_app.MainActivity
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity
import com.niccher.mpesa_analyzer_app.constants.Constants

class Splash : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var progressStatus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progress_bar)
        progressText = findViewById(R.id.progress_bar_text)

        progressText.visibility = View.GONE
        startLoading()
    }

    private fun startLoading() {
        Thread(Runnable {
            while (progressStatus < 100) {
                progressStatus += 1
                handler.post {
                    progressBar.progress = progressStatus
                    progressText.text = "$progressStatus%"
                }
                try {
                    Thread.sleep(25) // Adjust speed as needed
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            handler.post {
                progressText.visibility = View.GONE

                val currentUrl = com.niccher.mpesa_analyzer_app.helpers.AppPrefs.getBackendUrl(this@Splash)
                val intent = if (currentUrl.isEmpty()) {
                    Intent(this@Splash, SetupActivity::class.java)
                } else {
                    if (checkValidity() == "1") {
                        Intent(this@Splash, MainActivity::class.java)
                    } else {
                        Intent(this@Splash, com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity::class.java)
                    }
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            }
        }).start()
    }

    fun checkValidity(): String {
        val sharedPreferences = getSharedPreferences(Constants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        return sharedPreferences.getString("status", "3") ?: "3"
    }
}