package com.niccher.my_mpesa_analyzer

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private val handler = Handler()
    private var progressStatus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progress_bar)
        progressText = findViewById(R.id.progress_bar_text)

        progressText.visibility = View.GONE
        startLoading()
    }

    private fun startLoading(){
        Thread(Runnable {
            while (progressStatus < 100) {
                progressStatus += 1
                handler.post {
                    progressBar.progress = progressStatus
                    progressText.text = "$progressStatus%"
                }
                try {
                    Thread.sleep(50) // Adjust speed as needed
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            handler.post {
                progressText.visibility = View.GONE
            }
        }).start()
    }
}