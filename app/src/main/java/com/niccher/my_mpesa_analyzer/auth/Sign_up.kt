package com.niccher.my_mpesa_analyzer.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.interfaces.JsonAuthUser
import com.niccher.my_mpesa_analyzer.interfaces.JsonFonePrint
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import com.niccher.my_mpesa_analyzer.models.Mod_User_Auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Sign_up : AppCompatActivity() {

    private lateinit var btnSignup: Button
    private lateinit var txtHaveAccount: TextView
    private lateinit var regName: EditText
    private lateinit var regEml: EditText
    private lateinit var regPwd: EditText
    private lateinit var kon: Konstants
    private lateinit var gson: Gson

    private lateinit var jsonFonePrint: JsonFonePrint
    private lateinit var jsonAuthUser: JsonAuthUser

    private lateinit var pref_Auth: SharedPreferences
    private lateinit var sharedEditor: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnSignup = findViewById(R.id.btn_sign_register)

        txtHaveAccount = findViewById(R.id.id_back_login)

        regName = findViewById(R.id.ed_name)
        regEml = findViewById(R.id.ed_email)
        regPwd = findViewById(R.id.ed_pwd)

        kon = Konstants // Kotlin object, no need for 'new'

        pref_Auth = getSharedPreferences(kon.SHARED_AUTH_REGISTER, Context.MODE_PRIVATE)

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
                    val retrofit = Retrofit.Builder()
                        .baseUrl(kon.UPLOAD_AUTH_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(ServiceGenerators.getUnsafeOkHttpClient())
                        .build()

                    jsonAuthUser = retrofit.create(JsonAuthUser::class.java)
                    createUser(regNames, regEmls, regPwds)

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

    private fun createUser(new_name: String, new_eml: String, new_pwd: String) {
        sharedEditor = pref_Auth.edit()

        val parameters = mapOf(
            "varUsername" to new_name,
            "varEmail" to new_eml,
            "varPassword" to new_pwd
        )

        val call = jsonAuthUser.createRegister(parameters)

        call.enqueue(object : Callback<Mod_User_Auth> {
            override fun onResponse(call: Call<Mod_User_Auth>, response: Response<Mod_User_Auth>) {
                val postResponse = response.body()

                if (postResponse?.message.isNullOrEmpty() || postResponse?.status.isNullOrEmpty() || postResponse?.time.isNullOrEmpty()) {
                    // Handle empty response
                } else {
                    val message = postResponse?.message
                    val status = postResponse?.status
                    val time = postResponse?.time
                    val userid = postResponse?.userid

                    try {
                        when (status) {
                            "0", "2" -> {
                                Toast.makeText(
                                    this@Sign_up,
                                    message?.replace("var", ""),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            "1" -> {
                                sharedEditor.putString("status", status)
                                sharedEditor.putString("message", message)
                                sharedEditor.putString("time", time)
                                sharedEditor.putString("userid", userid)
                                sharedEditor.apply()

                                val to_home = Intent(this@Sign_up, Sign_In::class.java)
                                to_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out)
                                startActivity(to_home)
                                finish()
                            }
                        }
                    } catch (ex: Exception) {
                        Toast.makeText(
                            this@Sign_up,
                            "${ex.message}\nUnknown error occurred",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Mod_User_Auth>, t: Throwable) {
                Toast.makeText(
                    this@Sign_up,
                    "${t.message}\nUnknown error occurred, please try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}