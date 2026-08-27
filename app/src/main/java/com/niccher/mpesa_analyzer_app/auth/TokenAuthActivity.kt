package com.niccher.mpesa_analyzer_app.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.MainActivity
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.helpers.DeviceFingerprint
import com.niccher.mpesa_analyzer_app.api.AuthApiService
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.models.UserAuthModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Links the handset to a web-generated access token (typed or QR-scanned).
 * On success, immediately registers a device fingerprint so uploads have a valid varDevId.
 */
class TokenAuthActivity : AppCompatActivity() {

    private lateinit var edtToken: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var btnScanQr: Button

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            edtToken.setText(result.contents)
            verifyToken(result.contents.trim())
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
        btnSubmit.isEnabled = false
        btnScanQr.isEnabled = false
        Toast.makeText(this, "Verifying token…", Toast.LENGTH_SHORT).show()

        val map = HashMap<String, String>()
        map["token"] = token

        val service = ServiceGenerator.createService(AuthApiService::class.java, this)
        service.verifyToken(map).enqueue(object : Callback<UserAuthModel> {
            override fun onResponse(call: Call<UserAuthModel>, response: Response<UserAuthModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == "1") {
                        // Save session first
                        getSharedPreferences(Constants.SHARED_AUTH_LOGIN, MODE_PRIVATE).edit().apply {
                            putString("status", body.status)
                            putString("message", body.message)
                            putString("time", body.time)
                            putString("userid", body.userid)
                            putString("uuId", token)
                            putString("token", token)
                            putString("user_name", body.user_name)
                            putString("user_email", body.user_email)
                            apply()
                        }

                        Log.i(Constants.TAGGED, "Token verified — registering device fingerprint")
                        Toast.makeText(
                            this@TokenAuthActivity,
                            "Token OK — registering device…",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Register device fingerprint (token entry OR QR path both land here)
                        DeviceFingerprint.register(this@TokenAuthActivity) { ok, message ->
                            btnSubmit.isEnabled = true
                            btnScanQr.isEnabled = true
                            Log.i(Constants.TAGGED, "TokenAuth device register ok=$ok msg=$message")
                            if (ok) {
                                Toast.makeText(
                                    this@TokenAuthActivity,
                                    "Device linked successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                goToMain()
                            } else {
                                // Token is valid but device print failed — still allow entry,
                                // but warn; upload will re-try / block with a clear message.
                                Toast.makeText(
                                    this@TokenAuthActivity,
                                    "Linked, but device register failed: $message",
                                    Toast.LENGTH_LONG
                                ).show()
                                goToMain()
                            }
                        }
                    } else {
                        btnSubmit.isEnabled = true
                        btnScanQr.isEnabled = true
                        Toast.makeText(this@TokenAuthActivity, body.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    btnSubmit.isEnabled = true
                    btnScanQr.isEnabled = true
                    Toast.makeText(
                        this@TokenAuthActivity,
                        "Server error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserAuthModel>, t: Throwable) {
                btnSubmit.isEnabled = true
                btnScanQr.isEnabled = true
                Toast.makeText(
                    this@TokenAuthActivity,
                    "Network Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun goToMain() {
        startActivity(
            Intent(this@TokenAuthActivity, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }
        )
        finish()
    }
}
