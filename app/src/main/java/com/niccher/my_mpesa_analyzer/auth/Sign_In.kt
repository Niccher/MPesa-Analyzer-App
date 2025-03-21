package com.niccher.my_mpesa_analyzer.auth

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.my_mpesa_analyzer.MainActivity
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.konstants.Konstants

class Sign_In : AppCompatActivity() {

    private lateinit var btnSignin: Button
    private lateinit var btnSignup: Button
    private lateinit var btnProceed: Button
    private lateinit var lgEml: EditText
    private lateinit var lgPwd: EditText
    private lateinit var kon: Konstants
    private lateinit var prefAuth: android.content.SharedPreferences
    private lateinit var prefDevice: android.content.SharedPreferences
    private lateinit var gson: Gson
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        btnSignin = findViewById(R.id.btn_signIn)
        btnSignup = findViewById(R.id.btn_signUp)
        btnProceed = findViewById(R.id.btn_sign_proceed)

        lgEml = findViewById(R.id.id_email_EditText)
        lgPwd = findViewById(R.id.id_password_EditText)

        kon = Konstants // Kotlin object, no need for 'new'

        prefAuth = getSharedPreferences(kon.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        prefDevice = getSharedPreferences(kon.SHARED_DEVICE_ID, Context.MODE_PRIVATE)

        gson = GsonBuilder()
            .setLenient()
            .create()

        val builder = AlertDialog.Builder(this) // Use 'this' in Kotlin
        alertDialog = builder.create()

        btnSignin.setOnClickListener {
            val lgEmls = lgEml.text.toString().trim()
            val lgPwds = lgPwd.text.toString().trim()

            if (lgEmls.isEmpty() || lgPwds.isEmpty()) {
                Toast.makeText(this, "Both fields have to be filled", Toast.LENGTH_LONG).show()
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(lgEmls).matches()) {
                    Toast.makeText(this, "Email is not valid, please enter a valid email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Sending Data to test your creds", Toast.LENGTH_LONG).show()
//                    val retrofit = Retrofit.Builder()
//                        .baseUrl(kon.UPLOAD_AUTH_URL)
//                        .addConverterFactory(GsonConverterFactory.create(gson))
//                        .client(ServiceGenerator.getUnsafeOkHttpClient())
//                        .build()
//
//                    jsonAuthUser = retrofit.create(JsonAuthUser::class.java)
//                    createLogin(lgEmls, lgPwds)
                }
            }
        }

        btnSignup.setOnClickListener {
            startActivity(Intent(this, Sign_up::class.java).apply {
                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out)
            })
        }

        btnProceed.setOnClickListener {
            Toast.makeText(this, "Go to Landing page", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out)
            })
        }

    }
}