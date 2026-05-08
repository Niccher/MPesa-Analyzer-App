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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.niccher.my_mpesa_analyzer.helpers.MpesaParser

class Frag_Home : Fragment() {

    private lateinit var activity: AppCompatActivity
    private lateinit var kon: Konstants
    private lateinit var prefs: Prefs
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
    private lateinit var btn_view_insights: TextView

    private val CODE_READ_SMS = 102
    private val CODE_READ_STORAGE = 104
    private val CODE_WRITE_STORAGE = 106

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kon = Konstants
        prefs = Prefs()
        pref_loot_counter = requireActivity().getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)
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
        btn_view_insights = solv.findViewById(R.id.btn_view_insights)

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
            val intent = android.content.Intent(requireContext(), com.niccher.my_mpesa_analyzer.services.UploadService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(requireContext(), intent)
            android.widget.Toast.makeText(requireContext(), "Upload started in background", android.widget.Toast.LENGTH_SHORT).show()
        }

        last_time.text = prefs.getTimeStamp(requireActivity())

        btn_view_insights.setOnClickListener {
            showInsightsDialog()
        }

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

    private fun showInsightsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_insights, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        val chart = dialogView.findViewById<PieChart>(R.id.dialog_spending_chart)
        val closeBtn = dialogView.findViewById<TextView>(R.id.btn_close_dialog)
        val title = dialogView.findViewById<TextView>(R.id.dialog_title)
        val summaryTxt = dialogView.findViewById<TextView>(R.id.dialog_summary)

        title.text = "Insights: Last 10 Days"
        closeBtn.setOnClickListener { dialog.dismiss() }

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                if (e == null) return
                val entry = e as PieEntry
                chart.centerText = "${entry.label}\nKsh ${entry.value}"
            }

            override fun onNothingSelected() {
                chart.centerText = "Local Insights"
            }
        })

        updateChart(chart, summaryTxt)
        dialog.show()
    }

    private fun updateChart(chart: PieChart, summaryTxt: TextView) {
        val messages = mutableListOf<String>()
        val cr: ContentResolver = requireContext().contentResolver
        
        val tenDaysAgo = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000)
        val selection = "${Telephony.Sms.DATE} >= ?"
        val selectionArgs = arrayOf(tenDaysAgo.toString())
        
        val cursor = cr.query(Telephony.Sms.CONTENT_URI, arrayOf(Telephony.Sms.BODY), selection, selectionArgs, null)
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val body = cursor.getString(0)
                if (body.contains("MPESA", ignoreCase = true)) {
                    messages.add(body)
                }
            }
            cursor.close()
        }

        val spendingMap = MpesaParser.getSpendingByCategory(messages)
        val entries = mutableListOf<PieEntry>()
        val summaryBuilder = StringBuilder()
        
        for ((category, amount) in spendingMap) {
            if (amount > 0) {
                entries.add(PieEntry(amount, category))
                summaryBuilder.append("• $category: Ksh $amount\n")
            }
        }

        if (entries.isEmpty()) {
            chart.setNoDataText("No MPESA transactions found locally.")
            summaryTxt.text = "No transactions found in the last 10 days."
            chart.invalidate()
            return
        }

        summaryTxt.text = summaryBuilder.toString()

        val dataSet = PieDataSet(entries, "Spending Distribution")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = android.graphics.Color.BLACK

        val data = PieData(dataSet)
        chart.data = data
        chart.description.isEnabled = false
        chart.centerText = "Local Insights"
        chart.setEntryLabelColor(android.graphics.Color.BLACK)
        chart.animateY(1000)
        chart.invalidate()
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
}