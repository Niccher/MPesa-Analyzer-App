package com.niccher.mpesa_analyzer_app.fragments.Home

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
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.helpers.Encryptor
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.interfaces.JsonProcesses
import com.niccher.mpesa_analyzer_app.interfaces.JsonUploadLoot
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_My_Loot_Count
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
import com.niccher.mpesa_analyzer_app.helpers.MpesaParser

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
    private val CODE_POST_NOTIFICATIONS = 108

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
            requestNotificationPermissionIfNeeded()
        }

        text_get_and_upload.setOnClickListener {
            startFetchAndSync()
        }

        last_time.text = prefs.getTimeStamp(requireActivity())

        btn_view_insights.setOnClickListener {
            showInsightsDialog()
        }

        // Ensure device fingerprint exists if user already has a token from an older build
        ensureDeviceFingerprint()

        // Register receiver for upload complete
        val filter = android.content.IntentFilter(
            com.niccher.mpesa_analyzer_app.services.UploadService.ACTION_UPLOAD_COMPLETE
        )
        requireActivity().registerReceiver(uploadReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        requestNotificationPermissionIfNeeded()

        return solv
    }

    private val uploadReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
            if (intent?.action == com.niccher.mpesa_analyzer_app.services.UploadService.ACTION_UPLOAD_COMPLETE) {
                val success = intent.getBooleanExtra(
                    com.niccher.mpesa_analyzer_app.services.UploadService.EXTRA_SUCCESS,
                    false
                )
                val message = intent.getStringExtra(
                    com.niccher.mpesa_analyzer_app.services.UploadService.EXTRA_MESSAGE
                ) ?: ""
                val count = intent.getIntExtra(
                    com.niccher.mpesa_analyzer_app.services.UploadService.EXTRA_COUNT,
                    0
                )

                Log.i(kon.TAGGED, "Broadcast received: success=$success count=$count msg=$message")

                progressBar.visibility = View.GONE
                last_time.text = prefs.getTimeStamp(requireActivity())

                // Read the updated count from SharedPreferences
                val syncedCount = prefs.getPrefsAuth("loot_count", requireActivity())
                text_get_loot_count.text = "Synced $syncedCount times."
                Log.i(kon.TAGGED, "Updated UI: Synced $syncedCount times")

                // Refresh from server so count matches web
                calc_Loot()

                Toast.makeText(
                    requireContext(),
                    if (success) message.ifBlank { "Sync complete" } else "Sync failed: $message",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    CODE_POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun ensureDeviceFingerprint() {
        val token = prefs.getPrefsAuth("auth", requireContext())
        if (token.isBlank() || token == "nullable") return

        if (!com.niccher.mpesa_analyzer_app.helpers.DeviceFingerprint.isRegistered(requireContext())) {
            Log.i(kon.TAGGED, "Device fingerprint missing — registering from Home")
            com.niccher.mpesa_analyzer_app.helpers.DeviceFingerprint.register(requireContext()) { ok, msg ->
                Log.i(kon.TAGGED, "Home device register ok=$ok msg=$msg")
                if (!ok) {
                    Toast.makeText(
                        requireContext(),
                        "Device registration failed: $msg. Re-link with token/QR.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Log.d(kon.TAGGED, "Device fingerprint already registered")
        }
    }

    private fun startFetchAndSync() {
        // SMS permission
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("SMS permission required")
                setMessage("Grant SMS permission so the app can fetch MPESA messages.")
                setPositiveButton("Grant") { _, _ ->
                    requestPermissions(arrayOf(Manifest.permission.READ_SMS), CODE_READ_SMS)
                }
                setNegativeButton("Cancel", null)
                show()
            }
            return
        }

        val backendUrl = com.niccher.mpesa_analyzer_app.helpers.AppPrefs.getBackendUrl(requireContext())
        if (backendUrl.isBlank()) {
            Toast.makeText(requireContext(), "Backend URL not set. Open Setup first.", Toast.LENGTH_LONG).show()
            return
        }

        val token = prefs.getPrefsAuth("auth", requireContext())
        if (token.isBlank() || token == "nullable") {
            Toast.makeText(
                requireContext(),
                "Not linked. Open token login and enter/scan your access token.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val printId = prefs.getPrefsAuth("print", requireContext())
        Log.i(kon.TAGGED, "startFetchAndSync: printId='$printId'")
        if (printId.isBlank() || printId == "nullable") {
            Log.w(kon.TAGGED, "Device ID missing, attempting registration")
            Toast.makeText(requireContext(), "Registering device fingerprint…", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.VISIBLE
            com.niccher.mpesa_analyzer_app.helpers.DeviceFingerprint.register(requireContext()) { ok, msg ->
                progressBar.visibility = View.GONE
                Log.i(kon.TAGGED, "Device registration result: ok=$ok msg=$msg")
                if (ok) {
                    launchUploadService()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Device registration failed: $msg. Re-link with token/QR.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return
        }

        launchUploadService()
    }

    private fun launchUploadService() {
        progressBar.visibility = View.VISIBLE
        val intent = android.content.Intent(
            requireContext(),
            com.niccher.mpesa_analyzer_app.services.UploadService::class.java
        )
        androidx.core.content.ContextCompat.startForegroundService(requireContext(), intent)
        Toast.makeText(requireContext(), "Upload started in background", Toast.LENGTH_SHORT).show()
        Log.i(kon.TAGGED, "Fetch and Sync: UploadService started")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireActivity().unregisterReceiver(uploadReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
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