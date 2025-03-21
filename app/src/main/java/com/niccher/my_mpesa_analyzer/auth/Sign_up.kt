package com.niccher.my_mpesa_analyzer.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Sign_up : AppCompatActivity() {

    private lateinit var btnSignup: Button
    private lateinit var txtHaveAccount: TextView
    private lateinit var regName: EditText
    private lateinit var regEml: EditText
    private lateinit var regPwd: EditText
    private lateinit var kon: Konstants
    private lateinit var prefAuth: android.content.SharedPreferences
    private lateinit var gson: Gson


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnSignup = findViewById(R.id.btn_sign_register)

        txtHaveAccount = findViewById(R.id.id_back_login)

        regName = findViewById(R.id.ed_name)
        regEml = findViewById(R.id.ed_email)
        regPwd = findViewById(R.id.ed_pwd)

        kon = Konstants // Kotlin object, no need for 'new'

        prefAuth = getSharedPreferences(kon.SHARED_AUTH_REGISTER, Context.MODE_PRIVATE)

        gson = GsonBuilder()
            .setLenient()
            .create()

        btnSignup.setOnClickListener {
            val regNames = regName.text.toString().trim()
            val regEmls = regEml.text.toString().trim()
            val regPwds = regPwd.text.toString().trim()

            if (regNames.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
            if (regPwds.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
            if (regEmls.isEmpty()) {
                Toast.makeText(this, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                if (!Patterns.EMAIL_ADDRESS.matcher(regEmls).matches() && regNames.isNotEmpty() && regPwds.isNotEmpty()) {
                    Toast.makeText(this, "Email is not valid, please enter a valid email", Toast.LENGTH_SHORT).show()
                } else {
//                    val retrofit = Retrofit.Builder()
//                        .baseUrl(kon.UPLOAD_AUTH_URL)
//                        .addConverterFactory(GsonConverterFactory.create(gson))
//                        .client(ServiceGenerator.getUnsafeOkHttpClient())
//                        .build()
//
//                    jsonAuthUser = retrofit.create(JsonAuthUser::class.java)
//                    createteUser(regNames, regEmls, regPwds)

                    Log.e(kon.TAGGED, "onClick: Email as $regEmls")
                    Log.e(kon.TAGGED, "onClick: Name as $regNames")
                    Log.e(kon.TAGGED, "onClick: Passwd as $regPwds")
                }
            }
        }

        txtHaveAccount.setOnClickListener {
            val intent = Intent(this, Sign_In::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out)
        }

    }
}