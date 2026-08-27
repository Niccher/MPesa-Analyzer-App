package com.niccher.mpesa_analyzer_app.fragments.Home

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.helpers.Encryptor
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.api.FinancialApiService
import com.niccher.mpesa_analyzer_app.api.ProcessesApiService
import com.niccher.mpesa_analyzer_app.api.UploadLootApiService
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.models.FinancialOverview
import com.niccher.mpesa_analyzer_app.models.MyLootCountModel
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
import com.niccher.mpesa_analyzer_app.models.FinancialAnalystModel.FinancialHealthResponse
import com.niccher.mpesa_analyzer_app.models.FinancialAnalystModel.SmartAlertsResponse
import com.niccher.mpesa_analyzer_app.models.FinancialAnalystModel.SpendingTrendsResponse
import com.niccher.mpesa_analyzer_app.models.FinancialAnalystModel.RecurringPaymentsResponse
import androidx.cardview.widget.CardView
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var activity: AppCompatActivity
    private lateinit var kon: Constants
    private lateinit var prefs: Prefs
    private lateinit var sbsent: StringBuffer

    private lateinit var jsonProcesses: ProcessesApiService
    private lateinit var jsonFinancial: FinancialApiService
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

    private lateinit var kpiReceivedAmount: TextView
    private lateinit var kpiSentAmount: TextView
    private lateinit var kpiTotalTxns: TextView
    private lateinit var kpiTotalSenders: TextView
    private lateinit var categoryContainer: LinearLayout

    // Financial Analyst cards
    private lateinit var cardHealth: CardView
    private lateinit var cardTrends: CardView
    private lateinit var cardAlerts: CardView
    private lateinit var cardRecurring: CardView
    private lateinit var healthScoreValue: TextView
    private lateinit var healthScoreLabel: TextView
    private lateinit var healthScoreTip: TextView
    private lateinit var trendThisMonth: TextView
    private lateinit var trendLastMonth: TextView
    private lateinit var trendChangePct: TextView
    private lateinit var trendDirection: TextView
    private lateinit var alertsContainer: LinearLayout
    private lateinit var recurringContainer: LinearLayout

    private lateinit var homeViewModel: HomeViewModel

    private val CODE_READ_SMS = 102
    private val CODE_READ_STORAGE = 104
    private val CODE_WRITE_STORAGE = 106
    private val CODE_POST_NOTIFICATIONS = 108

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kon = Constants
        prefs = Prefs()
        pref_loot_counter = requireActivity().getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
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

        kpiReceivedAmount = solv.findViewById(R.id.kpi_received_amount)
        kpiSentAmount = solv.findViewById(R.id.kpi_sent_amount)
        kpiTotalTxns = solv.findViewById(R.id.kpi_total_txns)
        kpiTotalSenders = solv.findViewById(R.id.kpi_total_senders)
        categoryContainer = solv.findViewById(R.id.category_container)

        // Analyst card views
        cardHealth = solv.findViewById(R.id.card_health)
        cardTrends = solv.findViewById(R.id.card_trends)
        cardAlerts = solv.findViewById(R.id.card_alerts)
        cardRecurring = solv.findViewById(R.id.card_recurring)
        healthScoreValue = solv.findViewById(R.id.health_score_value)
        healthScoreLabel = solv.findViewById(R.id.health_score_label)
        healthScoreTip = solv.findViewById(R.id.health_score_tip)
        trendThisMonth = solv.findViewById(R.id.trend_this_month)
        trendLastMonth = solv.findViewById(R.id.trend_last_month)
        trendChangePct = solv.findViewById(R.id.trend_change_pct)
        trendDirection = solv.findViewById(R.id.trend_direction)
        alertsContainer = solv.findViewById(R.id.alerts_container)
        recurringContainer = solv.findViewById(R.id.recurring_container)

        perm_request.visibility = View.GONE
        progressBar.visibility = View.GONE

        reqPermission(Manifest.permission.READ_SMS, CODE_READ_SMS)

        pref_loot_counter = requireActivity().getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)

        calc_Loot()
        fetchFinancialOverview()
        fetchFinancialHealth()
        fetchSpendingTrends()
        fetchSmartAlerts()
        fetchRecurringPayments()

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

        ensureDeviceFingerprint()

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

                val syncedCount = prefs.getPrefsAuth("loot_count", requireActivity())
                text_get_loot_count.text = "Synced $syncedCount times."
                Log.i(kon.TAGGED, "Updated UI: Synced $syncedCount times")

                calc_Loot()
                fetchFinancialOverview()
                fetchFinancialHealth()
                fetchSpendingTrends()
                fetchSmartAlerts()
                fetchRecurringPayments()

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
            Toast.makeText(requireContext(), "Registering device fingerprint\u2026", Toast.LENGTH_SHORT).show()
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

    private fun fetchFinancialOverview() {
        jsonFinancial = ServiceGenerator.createService(FinancialApiService::class.java, requireContext())

        val parameters = mapOf(
            "varUser" to prefs.getPrefsAuth("auth", requireContext()),
            "varDev" to prefs.getPrefsAuth("print", requireActivity())
        )

        jsonFinancial.getFinancialOverview(parameters).enqueue(object : Callback<com.niccher.mpesa_analyzer_app.models.FinancialOverviewResponse> {
            override fun onResponse(
                call: Call<com.niccher.mpesa_analyzer_app.models.FinancialOverviewResponse>,
                response: Response<com.niccher.mpesa_analyzer_app.models.FinancialOverviewResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == 1 && body.overview != null) {
                        homeViewModel.setOverview(body.overview)
                        populateDashboard(body.overview)
                    }
                }
            }

            override fun onFailure(
                call: Call<com.niccher.mpesa_analyzer_app.models.FinancialOverviewResponse>,
                t: Throwable
            ) {
                Log.e(kon.TAGGED, "fetchFinancialOverview Error: ${t.message}")
            }
        })
    }

    private fun populateDashboard(overview: FinancialOverview) {
        val fmt = NumberFormat.getNumberInstance(Locale.US)
        fmt.minimumFractionDigits = 0
        fmt.maximumFractionDigits = 0

        kpiReceivedAmount.text = "Ksh ${fmt.format(overview.total_amount_received.toLong())}"
        kpiSentAmount.text = "Ksh ${fmt.format(overview.total_amount_sent.toLong())}"
        kpiTotalTxns.text = fmt.format(overview.total_transactions.toLong())
        kpiTotalSenders.text = fmt.format(overview.total_senders.toLong())

        buildCategoryBars(overview)
    }

    private fun buildCategoryBars(overview: FinancialOverview) {
        val breakdown = overview.category_breakdown ?: return
        val total = overview.total_transactions
        if (total == 0) return

        val density = resources.displayMetrics.density

        for (meta in HomeViewModel.CATEGORY_META) {
            val count = breakdown[meta.key] ?: 0
            if (count == 0) continue
            val pct = (count.toFloat() / total) * 100f

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, (6 * density).toInt(), 0, (6 * density).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val labelRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val colorDot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (10 * density).toInt(),
                    (10 * density).toInt()
                ).also {
                    it.setMargins(0, (2 * density).toInt(), (6 * density).toInt(), 0)
                }
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(resources.getColor(meta.colorRes, requireContext().theme))
                }
                background = bg
            }

            val nameTv = TextView(requireContext()).apply {
                text = meta.label
                textSize = 13f
                setTextColor(resources.getColor(R.color.text_primary, requireContext().theme))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val countTv = TextView(requireContext()).apply {
                text = "$count (${"%.0f".format(pct)}%)"
                textSize = 12f
                setTextColor(resources.getColor(R.color.text_secondary, requireContext().theme))
            }

            labelRow.addView(colorDot)
            labelRow.addView(nameTv)
            labelRow.addView(countTv)
            row.addView(labelRow)

            val barContainer = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (6 * density).toInt()
                ).also {
                    it.setMargins(0, (3 * density).toInt(), 0, 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadii = floatArrayOf(
                        (3 * density), (3 * density),
                        (3 * density), (3 * density),
                        (3 * density), (3 * density),
                        (3 * density), (3 * density)
                    )
                    setColor(resources.getColor(R.color.color_ui_divider, requireContext().theme))
                }
            }

            val barFill = View(requireContext()).apply {
                val widthFraction = (pct / 100f).coerceIn(0f, 1f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, widthFraction)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadii = floatArrayOf(
                        (3 * density), (3 * density),
                        (3 * density), (3 * density),
                        (3 * density), (3 * density),
                        (3 * density), (3 * density)
                    )
                    setColor(resources.getColor(meta.colorRes, requireContext().theme))
                }
            }

            barContainer.addView(barFill)
            row.addView(barContainer)

            val line = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).also {
                    it.topMargin = (6 * density).toInt()
                }
                setBackgroundColor(resources.getColor(R.color.color_ui_divider, requireContext().theme))
            }
            row.addView(line)

            categoryContainer.addView(row)
        }
    }

    private fun calc_Loot() {
        pref_loot_counter = requireActivity().getSharedPreferences(kon.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)

        val jsonProcesses = ServiceGenerator.createService(ProcessesApiService::class.java, requireContext())

        val parameters = mapOf(
            "varUser" to prefs.getPrefsAuth("auth", requireContext()),
            "varDev" to prefs.getPrefsAuth("print", requireActivity())
        )

        val call = jsonProcesses.getLootCount(parameters)
        call.enqueue(object : Callback<MyLootCountModel> {
            override fun onResponse(call: Call<MyLootCountModel>, response: Response<MyLootCountModel>) {
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

            override fun onFailure(call: Call<MyLootCountModel>, t: Throwable) {
                Log.e(kon.TAGGED, "calc_Loot Error")
                Log.e(kon.TAGGED, t.message ?: "Unknown error")
            }
        })
    }

    // ─── Financial Analyst Endpoints ──────────────────────────────────────────

    private fun fetchFinancialHealth() {
        val params = mapOf(
            "varUser" to prefs.getPrefsAuth("auth", requireContext()),
            "varDev" to prefs.getPrefsAuth("print", requireActivity())
        )
        val api = ServiceGenerator.createService(FinancialApiService::class.java, requireContext())
        api.getFinancialHealth(params).enqueue(object : Callback<FinancialHealthResponse> {
            override fun onResponse(call: Call<FinancialHealthResponse>, response: Response<FinancialHealthResponse>) {
                if (!isAdded) return
                val body = response.body() ?: return
                if (body.status != 1) return
                val fmt = NumberFormat.getNumberInstance(Locale.US)
                healthScoreValue.text = "${body.score}"
                healthScoreLabel.text = when {
                    body.score >= 80 -> "Excellent — You're in great shape"
                    body.score >= 60 -> "Good — Minor improvements possible"
                    body.score >= 40 -> "Fair — Review your spending"
                    else -> "Poor — Immediate action needed"
                }
                healthScoreTip.text = body.tips.firstOrNull() ?: ""
                // colour-code the score badge
                val colorRes = when {
                    body.score >= 80 -> R.color.semantic_success
                    body.score >= 60 -> R.color.brand_primary
                    body.score >= 40 -> R.color.semantic_warning
                    else -> R.color.semantic_danger
                }
                val bg = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(resources.getColor(colorRes, requireContext().theme))
                }
                healthScoreValue.background = bg
                cardHealth.visibility = View.VISIBLE
            }
            override fun onFailure(call: Call<FinancialHealthResponse>, t: Throwable) {
                Log.e(kon.TAGGED, "fetchFinancialHealth error: ${t.message}")
            }
        })
    }

    private fun fetchSpendingTrends() {
        val params = mapOf(
            "varUser" to prefs.getPrefsAuth("auth", requireContext()),
            "varDev" to prefs.getPrefsAuth("print", requireActivity())
        )
        val api = ServiceGenerator.createService(FinancialApiService::class.java, requireContext())
        api.getSpendingTrends(params).enqueue(object : Callback<SpendingTrendsResponse> {
            override fun onResponse(call: Call<SpendingTrendsResponse>, response: Response<SpendingTrendsResponse>) {
                if (!isAdded) return
                val body = response.body() ?: return
                if (body.status != 1 || body.trends == null) return
                val t = body.trends
                val fmt = NumberFormat.getNumberInstance(Locale.US)
                fmt.minimumFractionDigits = 0; fmt.maximumFractionDigits = 0
                trendThisMonth.text = "Ksh ${fmt.format(t.this_month.toLong())}"
                trendLastMonth.text = "Ksh ${fmt.format(t.last_month.toLong())}"
                val pct = t.percentage
                val isUp = t.trend == "up"
                val arrow = if (isUp) "▲" else "▼"
                val colorRes = if (isUp) R.color.semantic_danger else R.color.semantic_success
                trendChangePct.text = "$arrow ${"%.1f".format(pct)}%"
                trendChangePct.setTextColor(resources.getColor(colorRes, requireContext().theme))
                trendDirection.text = if (isUp) "more than last month" else "less than last month"
                cardTrends.visibility = View.VISIBLE
            }
            override fun onFailure(call: Call<SpendingTrendsResponse>, t: Throwable) {
                Log.e(kon.TAGGED, "fetchSpendingTrends error: ${t.message}")
            }
        })
    }

    private fun fetchSmartAlerts() {
        val params = mapOf(
            "varUser" to prefs.getPrefsAuth("auth", requireContext()),
            "varDev" to prefs.getPrefsAuth("print", requireActivity())
        )
        val api = ServiceGenerator.createService(FinancialApiService::class.java, requireContext())
        api.getSmartAlerts(params).enqueue(object : Callback<SmartAlertsResponse> {
            override fun onResponse(call: Call<SmartAlertsResponse>, response: Response<SmartAlertsResponse>) {
                if (!isAdded) return
                val body = response.body() ?: return
                if (body.status != 1 || body.alerts.isNullOrEmpty()) return
                val density = resources.displayMetrics.density
                alertsContainer.removeAllViews()
                body.alerts.take(5).forEach { alert ->
                    val colorRes = when (alert.level) {
                        "high" -> R.color.semantic_danger
                        "medium" -> R.color.semantic_warning
                        else -> R.color.brand_primary
                    }
                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    val titleTv = TextView(requireContext()).apply {
                        text = alert.title
                        textSize = 13f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(resources.getColor(colorRes, requireContext().theme))
                    }
                    val msgTv = TextView(requireContext()).apply {
                        text = alert.message
                        textSize = 12f
                        setTextColor(resources.getColor(R.color.text_secondary, requireContext().theme))
                    }
                    row.addView(titleTv)
                    row.addView(msgTv)
                    alertsContainer.addView(row)
                }
                cardAlerts.visibility = View.VISIBLE
            }
            override fun onFailure(call: Call<SmartAlertsResponse>, t: Throwable) {
                Log.e(kon.TAGGED, "fetchSmartAlerts error: ${t.message}")
            }
        })
    }

    private fun fetchRecurringPayments() {
        val params = mapOf(
            "varUser" to prefs.getPrefsAuth("auth", requireContext()),
            "varDev" to prefs.getPrefsAuth("print", requireActivity())
        )
        val api = ServiceGenerator.createService(FinancialApiService::class.java, requireContext())
        api.getRecurringPayments(params).enqueue(object : Callback<RecurringPaymentsResponse> {
            override fun onResponse(call: Call<RecurringPaymentsResponse>, response: Response<RecurringPaymentsResponse>) {
                if (!isAdded) return
                val body = response.body() ?: return
                if (body.status != 1 || body.payments.isNullOrEmpty()) return
                val fmt = NumberFormat.getNumberInstance(Locale.US)
                fmt.minimumFractionDigits = 0; fmt.maximumFractionDigits = 0
                val density = resources.displayMetrics.density
                recurringContainer.removeAllViews()
                body.payments.take(5).forEach { payment ->
                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        setPadding(0, (6 * density).toInt(), 0, (6 * density).toInt())
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    val nameTv = TextView(requireContext()).apply {
                        text = payment.counterparty
                        textSize = 13f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(resources.getColor(R.color.text_primary, requireContext().theme))
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    val amtTv = TextView(requireContext()).apply {
                        text = "Ksh ${fmt.format(payment.amount.toLong())}"
                        textSize = 13f
                        setTextColor(resources.getColor(R.color.semantic_danger, requireContext().theme))
                        gravity = android.view.Gravity.END
                    }
                    val occursTv = TextView(requireContext()).apply {
                        text = " · ${payment.occurs}×"
                        textSize = 11f
                        setTextColor(resources.getColor(R.color.text_secondary, requireContext().theme))
                    }
                    row.addView(nameTv)
                    row.addView(amtTv)
                    row.addView(occursTv)
                    recurringContainer.addView(row)
                }
                cardRecurring.visibility = View.VISIBLE
            }
            override fun onFailure(call: Call<RecurringPaymentsResponse>, t: Throwable) {
                Log.e(kon.TAGGED, "fetchRecurringPayments error: ${t.message}")
            }
        })
    }
}

