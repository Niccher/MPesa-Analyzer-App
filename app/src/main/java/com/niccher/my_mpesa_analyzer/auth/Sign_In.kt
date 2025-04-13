package com.niccher.my_mpesa_analyzer.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.my_mpesa_analyzer.MainActivity
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.auth.Sign_up
import com.niccher.my_mpesa_analyzer.interfaces.JsonAuthUser
import com.niccher.my_mpesa_analyzer.interfaces.JsonFonePrint
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import com.niccher.my_mpesa_analyzer.models.Mod_Fone_Id
import com.niccher.my_mpesa_analyzer.models.Mod_User_Auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Sign_In : AppCompatActivity() {

    private lateinit var btnSignin: Button
    private lateinit var btnSignup: Button
    private lateinit var btnProceed: Button
    private lateinit var lgEml: EditText
    private lateinit var lgPwd: EditText

    private lateinit var jsonFonePrint: JsonFonePrint
    private lateinit var jsonAuthUser: JsonAuthUser
    private lateinit var kon: Konstants
    private lateinit var prefAuth: SharedPreferences
    private lateinit var prefDevice: SharedPreferences
    private lateinit var sharedEditor: SharedPreferences.Editor
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

        kon = Konstants
        prefAuth = getSharedPreferences(kon.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        prefDevice = getSharedPreferences(kon.SHARED_DEVICE_ID, Context.MODE_PRIVATE)

        gson = GsonBuilder().setLenient().create()

        val builder = AlertDialog.Builder(this)
        alertDialog = builder.create()

        checkPrint()

        btnSignin.setOnClickListener {
            val email = lgEml.text.toString().trim()
            val password = lgPwd.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() ->
                    Toast.makeText(this, "Both fields have to be filled", Toast.LENGTH_LONG).show()
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    Toast.makeText(this, "Email is not valid, please enter a valid email", Toast.LENGTH_LONG).show()
                else -> {
                    val retrofit = Retrofit.Builder()
                        .baseUrl(kon.UPLOAD_AUTH_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(ServiceGenerators.getUnsafeOkHttpClient())
                        .build()

                    jsonAuthUser = retrofit.create(JsonAuthUser::class.java)
                    createLogin(email, password)
                }
            }
        }

        btnSignup.setOnClickListener {
            startActivity(Intent(this, Sign_up::class.java))
            overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out)
        }

        btnProceed.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out)
        }
    }

    private fun createPrint() {
        val parameters = mapOf(
            "device_Board" to Build.BOARD,
            "device_Bootloader" to Build.BOOTLOADER,
            "device_Brand" to Build.BRAND,
            "device_Device" to Build.DEVICE,
            "device_Display" to Build.DISPLAY,
            "device_Fingerprint" to Build.FINGERPRINT,
            "device_Hardware" to Build.HARDWARE,
            "device_Host" to Build.HOST,
            "device_Manufacturer" to Build.MANUFACTURER,
            "device_Model" to Build.MODEL,
            "device_Product" to Build.PRODUCT,
            "device_Tags" to Build.TAGS,
            "device_Type" to Build.TYPE,
            "device_User" to Build.USER,
            "device_Time" to Build.TIME.toString(),
            "device_Serial" to Build.SERIAL
        )

        jsonFonePrint.createPrint(parameters).enqueue(object : Callback<Mod_Fone_Id> {
            override fun onResponse(call: Call<Mod_Fone_Id>, response: Response<Mod_Fone_Id>) {
                response.body()?.let {
                    sharedEditor = prefDevice.edit()
                    sharedEditor.putString("status", it.status)
                    sharedEditor.putString("time", it.time)
                    sharedEditor.putString("message", it.message)
                    sharedEditor.putString("print_id", it.print_id)
                    sharedEditor.apply()
                }
            }

            override fun onFailure(call: Call<Mod_Fone_Id>, t: Throwable) {
                Log.e(kon.TAGGED, "Mod_Fone_Print onFailure: ${t.message}")
                Toast.makeText(this@Sign_In, "${t.message}\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun checkPrint() {
        val state = prefDevice.getString("status", "3")?.toIntOrNull()
        if (state != 1) {
            val retrofit = Retrofit.Builder()
                .baseUrl(kon.LINK_PROCESS)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ServiceGenerators.getUnsafeOkHttpClient())
                .build()

            jsonFonePrint = retrofit.create(JsonFonePrint::class.java)
            createPrint()
        }
    }

    private fun createLogin(email: String, password: String) {
        val parameters = mapOf("varEmail" to email, "varPassword" to password)

        jsonAuthUser.createLogin(parameters).enqueue(object : Callback<Mod_User_Auth> {
            override fun onResponse(call: Call<Mod_User_Auth>, response: Response<Mod_User_Auth>) {
                response.body()?.let {
                    if (it.status == "1") {
                        sharedEditor = prefAuth.edit()
                        sharedEditor.putString("status", it.status)
                        sharedEditor.putString("message", it.message)
                        sharedEditor.putString("time", it.time)
                        sharedEditor.putString("userId", it.userid)
                        sharedEditor.putString("uuId", it.uuid)
                        sharedEditor.putString("userName", it.user_name)
                        sharedEditor.putString("userEmail", it.user_email)
                        sharedEditor.apply()

                        val intent = Intent(this@Sign_In, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Sign_In, it.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<Mod_User_Auth>, t: Throwable) {
                Toast.makeText(this@Sign_In, "${t.message}\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show()
            }
        })
    }
}
