package com.niccher.mpesa_analyzer_app.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.MainActivity
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.interfaces.JsonAuthUser
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_User_Auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TokenAuthActivity : AppCompatActivity() {

    private lateinit var edtToken: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var btnScanQr: Button

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            edtToken.setText(result.contents)
            verifyToken(result.contents)
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token_auth)

        edtToken = findViewById(R.id.edt_token)
        btnSubmit = findViewById(R.id.btn_submit_token)
        btnScanQr = findViewById(R.id.btn_scan_qr)

        btnSubmit.setOnClickListener {
            val token = edtToken.text.toString().trim()
            if (token.isNotEmpty()) {
                verifyToken(token)
            } else {
                Toast.makeText(this, "Please enter a token", Toast.LENGTH_SHORT).show()
            }
        }

        btnScanQr.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan QR Code to Link Device")
                setBeepEnabled(true)
                setOrientationLocked(true)
            }
            barcodeLauncher.launch(options)
        }
    }

    private fun verifyToken(token: String) {
        val map = HashMap<String, String>()
        map["token"] = token

        val service = ServiceGenerators.createService(JsonAuthUser::class.java, this)
        service.verifyToken(map).enqueue(object : Callback<Mod_User_Auth> {
            override fun onResponse(call: Call<Mod_User_Auth>, response: Response<Mod_User_Auth>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "1") {
                        Toast.makeText(this@TokenAuthActivity, "Device Linked Successfully!", Toast.LENGTH_SHORT).show()

                        // Save session to SharedPreferences
                        getSharedPreferences(Konstants.SHARED_AUTH_LOGIN, MODE_PRIVATE).edit().apply {
                            putString("status", body.status)
                            putString("message", body.message)
                            putString("time", body.time)
                            putString("userid", body.userid)
                            putString("uuId", token) // Overwrite placeholder with the real token so Prefs.kt loads it properly
                            putString("token", token) // Also save explicitly as token
                            putString("user_name", body.user_name)
                            putString("user_email", body.user_email)
                            apply()
                        }

                        startActivity(
                            Intent(this@TokenAuthActivity, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                        finish()
                    } else {
                        Toast.makeText(this@TokenAuthActivity, body.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@TokenAuthActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Mod_User_Auth>, t: Throwable) {
                Toast.makeText(this@TokenAuthActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
