package com.niccher.mpesa_analyzer_app.splash

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.MainActivity
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val etBackendUrl = findViewById<EditText>(R.id.etBackendUrl)
        val btnTestConnection = findViewById<Button>(R.id.btnTestConnection)
        val tvTestResult = findViewById<TextView>(R.id.tvTestResult)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val currentUrl = AppPrefs.getBackendUrl(this)
        if (currentUrl.isNotEmpty()) {
            etBackendUrl.setText(currentUrl)
        }

        btnTestConnection.setOnClickListener {
            val url = etBackendUrl.text.toString().trim()
            if (url.isEmpty()) {
                tvTestResult.text = "Please enter a URL"
                tvTestResult.setTextColor(Color.RED)
                return@setOnClickListener
            }

            tvTestResult.text = "Testing connection..."
            tvTestResult.setTextColor(Color.GRAY)

            CoroutineScope(Dispatchers.IO).launch {
                var finalUrl = url
                if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                    finalUrl = "http://$finalUrl"
                }

                fun testUrl(testUrl: String): Boolean {
                    return try {
                        val client = ServiceGenerator.getUnsafeOkHttpClient(this@SetupActivity)
                        val request = Request.Builder().url(testUrl).head().build()
                        val response = client.newCall(request).execute()
                        response.isSuccessful || response.code in 200..499
                    } catch (e: Exception) {
                        false
                    }
                }

                try {
                    var success = testUrl(finalUrl)

                    if (!success && finalUrl.startsWith("http://")) {
                        val httpsUrl = finalUrl.replaceFirst("http://", "https://")
                        if (testUrl(httpsUrl)) {
                            finalUrl = httpsUrl
                            success = true
                        }
                    } else if (!success && finalUrl.startsWith("https://")) {
                        val httpUrl = finalUrl.replaceFirst("https://", "http://")
                        if (testUrl(httpUrl)) {
                            finalUrl = httpUrl
                            success = true
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (success) {
                            etBackendUrl.setText(finalUrl) // Update UI with correct protocol
                            tvTestResult.text = "Connection Successful!"
                            tvTestResult.setTextColor(Color.GREEN)
                        } else {
                            tvTestResult.text = "Failed to connect to the server."
                            tvTestResult.setTextColor(Color.RED)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvTestResult.text = "Failed: ${e.message}"
                        tvTestResult.setTextColor(Color.RED)
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            var url = etBackendUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://$url"
                }
                AppPrefs.setBackendUrl(this, url)
                
                val sharedPreferences = getSharedPreferences(Constants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
                val status = sharedPreferences.getString("status", "3") ?: "3"
                
                val intent = if (status == "1") {
                    Intent(this, MainActivity::class.java)
                } else {
                    Intent(this, com.niccher.mpesa_analyzer_app.auth.TokenAuthActivity::class.java)
                }
                
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                tvTestResult.text = "URL cannot be empty!"
                tvTestResult.setTextColor(Color.RED)
            }
        }
    }
}
