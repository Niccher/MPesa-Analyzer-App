package com.niccher.my_mpesa_analyzer.splash

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
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.auth.Sign_up
import com.niccher.my_mpesa_analyzer.konstants.Konstants

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

                val loginStatus = checkValidity(this, Konstants.SHARED_AUTH_LOGIN)
                Log.e(Konstants.TAGGED, "login_status is $loginStatus")
                var go_to = Intent()

                if (!loginStatus) {
                    Log.e(Konstants.TAGGED, "login_status is $loginStatus as NOT_SET")
                    go_to = Intent(this, Sign_up::class.java)
                } else {
                    //go_to = Intent(this, Sign_In::class.java)
                    Log.e(Konstants.TAGGED, "login_status is $loginStatus  as SET_ALREADY")
                }

                try {
                    startActivity(go_to)
                }catch (e: Exception) {
                    Log.e(Konstants.TAGGED, "An unexpected error occurred: ${e.message}")
                }

            }
        }).start()
    }

    fun checkValidity(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(key, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}