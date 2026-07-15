package com.niccher.mpesa_analyzer_app.fragments.Graph

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.databinding.FragGraphBinding
import com.niccher.mpesa_analyzer_app.fragments.Graph.Frag_Graph_VM.ChartDataItem
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.helpers.SummaryResponse
import com.niccher.mpesa_analyzer_app.interfaces.JsonProcesses
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_Summaries
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class Frag_Graph : Fragment() {

    private var _binding: FragGraphBinding? = null
    private val binding get() = _binding!!

    // Views
    private lateinit var connState: TextView
    private lateinit var connWait: View
    private lateinit var toolbarTitle: TextView
    private lateinit var chartTypeContainer: View
    private lateinit var iconChartType: ImageView
    private lateinit var txtChartType: TextView
    private lateinit var iconDropdown: ImageView
    private lateinit var kpiTotalReceived: TextView
    private lateinit var kpiTotalSent: TextView

    // Dependencies
    private lateinit var jsonProcesses: JsonProcesses
    private lateinit var kon: Konstants
    private lateinit var pref: Prefs
    private var gson: Gson = GsonBuilder().setLenient().create()
    private var summariesList: ArrayList<Mod_Summaries> = ArrayList()

    // ViewModel
    private val viewModel: Frag_Graph_VM by viewModels()

    // Chart state
    private var currentChartType: ChartType = ChartType.STACKED_BAR

    enum class ChartType {
        STACKED_BAR, LINE_GRAPH
    }

    // Colors
    private val colorReceived = Color.parseColor("#FF4CAF50") // Green
    private val colorSent = Color.parseColor("#FFF44336")     // Red
    private val colorUnknown = Color.parseColor("#FF9E9E9E")  // Gray

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        kon = Konstants
        pref = Prefs()

        _binding = FragGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        setupToolbar()
        setupToggleButton()
        setupChartObservers()

        getSummaries()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeViews() {
        connState = binding.connNoInternet
        connWait = binding.connWaitInternet
        iconChartType = binding.iconChartType
        txtChartType = binding.txtChartType
        iconDropdown = binding.iconDropdown
        kpiTotalReceived = binding.kpiTotalReceived
        kpiTotalSent = binding.kpiTotalSent
        chartTypeContainer = binding.chartTypeContainer
        toolbarTitle = binding.toolbarTitle

        // Hide connection elements initially
        connWait.visibility = View.GONE
        connState.visibility = View.GONE
    }

    private fun setupToolbar() {
        binding.toolbarTitle.text = "Financial Analytics"
    }

    private fun setupToggleButton() {
        // Set initial icon
        updateChartTypeIcon()

        // Set click listener on the entire container
        chartTypeContainer.setOnClickListener {
            toggleChartType()
        }

        // Add ripple effect programmatically
        chartTypeContainer.isClickable = true
        chartTypeContainer.isFocusable = true
    }

    private fun toggleChartType() {
        currentChartType = when (currentChartType) {
            ChartType.STACKED_BAR -> ChartType.LINE_GRAPH
            ChartType.LINE_GRAPH -> ChartType.STACKED_BAR
        }
        updateToolbarTitle()
        updateChartTypeIcon()
        refreshChartsWithCurrentData()
    }

    private fun updateToolbarTitle() {
        // No longer updating title based on chart type in the new layout
    }

    private fun updateChartTypeIcon() {
        val (iconRes, label) = when (currentChartType) {
            ChartType.STACKED_BAR -> R.drawable.ic_bar_chart to "Bar"
            ChartType.LINE_GRAPH -> R.drawable.ic_line_chart to "Line"
        }
        iconChartType.setImageResource(iconRes)
        txtChartType.text = label
    }

    private fun setupChartObservers() {
        viewModel.summaryData.observe(viewLifecycleOwner) { dataList ->
            if (dataList.isNotEmpty()) {
                refreshCharts(dataList)
            } else {
                showNoDataState()
            }
        }
    }

    private fun refreshChartsWithCurrentData() {
        viewModel.summaryData.value?.let { dataList ->
            if (dataList.isNotEmpty()) {
                refreshCharts(dataList)
            }
        }
    }

    private fun refreshCharts(dataList: List<Frag_Graph_VM.SummaryEntry>) {
        hideConnectionState()
        updateKPICards(dataList)

        when (currentChartType) {
            ChartType.STACKED_BAR -> showStackedBarChart(dataList)
            ChartType.LINE_GRAPH -> showLineChart(dataList)
        }
    }

    private fun updateKPICards(dataList: List<Frag_Graph_VM.SummaryEntry>) {
        var totalReceived = 0
        var totalSent = 0
        
        dataList.forEach {
            totalReceived += it.received.toInt()
            totalSent += it.sent.toInt()
        }
        
        kpiTotalReceived.text = "$totalReceived Times"
        kpiTotalSent.text = "$totalSent Times"
    }

    private fun showStackedBarChart(dataList: List<Frag_Graph_VM.SummaryEntry>) {
        binding.lineChart.visibility = View.GONE
        binding.barChart.visibility = View.VISIBLE

        val entries = dataList.mapIndexed { index, item ->
            BarEntry(index.toFloat(), floatArrayOf(item.received, item.sent, item.unknown))
        }

        val dataSet = BarDataSet(entries, "").apply {
            setColors(colorReceived, colorSent, colorUnknown)
            stackLabels = arrayOf("Received", "Sent", "Unknown")
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getBarStackedLabel(value: Float, entry: BarEntry?): String {
                    return if (value > 0) value.toInt().toString() else ""
                }
            }
        }

        binding.barChart.apply {
            // Clear previous data
            clear()

            // Data configuration
            data = BarData(dataSet).apply {
                barWidth = 0.6f
                setValueTextColor(Color.BLACK)
                setValueTextSize(10f)
            }

            // FIX: Chart padding to ensure X-axis is visible
            setExtraOffsets(20f, 20f, 20f, 40f) // Left, Top, Right, Bottom (extra space for X-axis)

            // Chart styling
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(60)
            setPinchZoom(true)
            setDrawGridBackground(false)
            setDrawBorders(true)
            setBorderColor(Color.LTGRAY)
            setBorderWidth(1f)

            // Description
            description.isEnabled = true
            description.text = "Transaction Breakdown Over Time"
            description.textSize = 12f
            description.textColor = Color.DKGRAY
            description.setPosition(description.xOffset, description.yOffset + 20f) // Move description up

            // Legend configuration
            legend.isEnabled = false
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.setDrawInside(false)
            legend.textSize = 12f
            legend.textColor = Color.BLACK
            legend.form = Legend.LegendForm.SQUARE
            legend.formSize = 12f
            legend.yOffset = 10f // Move legend down a bit

            // FIX: X-axis configuration for better visibility
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dataList.map { it.date })
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                gridLineWidth = 1f
                setDrawAxisLine(true)
                axisLineColor = Color.DKGRAY
                axisLineWidth = 2f // Thicker axis line
                labelCount = dataList.size.coerceAtMost(6)
                textSize = 11f
                textColor = Color.BLACK
                yOffset = 8f // Move labels down
                setAvoidFirstLastClipping(true) // Prevent first/last label clipping
                setCenterAxisLabels(false)

                // Ensure labels are not cut off
                setLabelCount(dataList.size, true)
            }

            // Y-axis configuration
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1000f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                gridLineWidth = 1f
                setDrawAxisLine(true)
                axisLineColor = Color.DKGRAY
                axisLineWidth = 2f
                textSize = 10f
                textColor = Color.BLACK
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000 -> "${(value / 1000).toInt()}K"
                            else -> value.toInt().toString()
                        }
                    }
                }
                xOffset = 10f // Move Y-axis labels left a bit
            }

            axisRight.isEnabled = false

            // Interaction
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDoubleTapToZoomEnabled(true)

            // FIX: Ensure chart fits properly
            fitScreen()
            setVisibleXRangeMaximum(6f) // Show reasonable number of bars

            // Animation
            animateY(1000)

            invalidate()
        }
    }

    private fun showLineChart(dataList: List<Frag_Graph_VM.SummaryEntry>) {
        binding.barChart.visibility = View.GONE
        binding.lineChart.visibility = View.VISIBLE

        val receivedEntries = mutableListOf<Entry>()
        val sentEntries = mutableListOf<Entry>()
        val unknownEntries = mutableListOf<Entry>()
        val dates = mutableListOf<String>()

        dataList.forEachIndexed { index, item ->
            receivedEntries.add(Entry(index.toFloat(), item.received))
            sentEntries.add(Entry(index.toFloat(), item.sent))
            unknownEntries.add(Entry(index.toFloat(), item.unknown))
            dates.add(item.date)
        }

        val receivedSet = LineDataSet(receivedEntries, "Received").apply {
            color = colorReceived
            circleRadius = 4f
            lineWidth = 3f
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            setCircleColor(colorReceived)
            circleHoleColor = Color.WHITE
            circleHoleRadius = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        val sentSet = LineDataSet(sentEntries, "Sent").apply {
            color = colorSent
            circleRadius = 4f
            lineWidth = 3f
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            setCircleColor(colorSent)
            circleHoleColor = Color.WHITE
            circleHoleRadius = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        val unknownSet = LineDataSet(unknownEntries, "Unknown").apply {
            color = colorUnknown
            circleRadius = 4f
            lineWidth = 3f
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            setCircleColor(colorUnknown)
            circleHoleColor = Color.WHITE
            circleHoleRadius = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        binding.lineChart.apply {
            // Clear previous data
            clear()

            // Data configuration
            data = LineData(receivedSet, sentSet, unknownSet).apply {
                setValueTextSize(10f)
                setValueTextColor(Color.BLACK)
            }

            // FIX: Chart padding to ensure X-axis is visible
            setExtraOffsets(20f, 20f, 20f, 40f) // Left, Top, Right, Bottom (extra space for X-axis)

            // Chart styling
            setDrawGridBackground(false)
            setDrawBorders(true)
            setBorderColor(Color.LTGRAY)
            setBorderWidth(1f)

            // Description
            description.isEnabled = true
            description.text = "Transaction Trends Over Time"
            description.textSize = 12f
            description.textColor = Color.DKGRAY
            description.setPosition(description.xOffset, description.yOffset + 20f) // Move description up

            // Legend configuration
            legend.isEnabled = false
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.setDrawInside(false)
            legend.textSize = 12f
            legend.textColor = Color.BLACK
            legend.form = Legend.LegendForm.LINE
            legend.formSize = 12f
            legend.yOffset = 10f // Move legend down a bit

            // FIX: X-axis configuration for better visibility
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dates)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                gridLineWidth = 1f
                setDrawAxisLine(true)
                axisLineColor = Color.DKGRAY
                axisLineWidth = 2f // Thicker axis line
                labelCount = dataList.size.coerceAtMost(6)
                textSize = 11f
                textColor = Color.BLACK
                yOffset = 8f // Move labels down
                setAvoidFirstLastClipping(true) // Prevent first/last label clipping
                setCenterAxisLabels(false)

                // Ensure labels are not cut off
                setLabelCount(dataList.size, true)
            }

            // Y-axis configuration
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1000f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                gridLineWidth = 1f
                setDrawAxisLine(true)
                axisLineColor = Color.DKGRAY
                axisLineWidth = 2f
                textSize = 10f
                textColor = Color.BLACK
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000 -> "${(value / 1000).toInt()}K"
                            else -> value.toInt().toString()
                        }
                    }
                }
                xOffset = 10f // Move Y-axis labels left a bit
            }

            axisRight.isEnabled = false

            // Interaction
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDoubleTapToZoomEnabled(true)

            // FIX: Ensure chart fits properly
            fitScreen()
            setVisibleXRangeMaximum(6f) // Show reasonable number of points

            // Animation
            animateXY(1000, 1000)

            invalidate()
        }
    }

    private fun showNoDataState() {
        binding.barChart.visibility = View.GONE
        binding.lineChart.visibility = View.GONE
        connState.visibility = View.VISIBLE
        connState.text = getString(R.string.no_data_available)
    }

    private fun hideConnectionState() {
        connWait.visibility = View.GONE
        connState.visibility = View.GONE
    }

    private fun isConnected(): Boolean {
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo?.isConnected == true
    }

    private fun showLoadingState() {
        binding.connWaitInternet.visibility = View.VISIBLE
        connState.visibility = View.GONE
        binding.barChart.visibility = View.GONE
        binding.lineChart.visibility = View.GONE
    }

    private fun showOfflineState() {
        binding.connWaitInternet.visibility = View.GONE
        connState.visibility = View.VISIBLE
        connState.text = getString(R.string.str_no_internet_connection)
        binding.barChart.visibility = View.GONE
        binding.lineChart.visibility = View.GONE
    }

    private fun getSummaries() {
        val jsonProcesses = ServiceGenerators.createService(JsonProcesses::class.java, requireContext())

        showLoadingState()

        val params = mapOf(
            "varUser" to pref.getPrefsAuth("auth", requireContext()),
            "varDev" to pref.getPrefsAuth("print", requireActivity())
        )

        jsonProcesses.getSummary(params).enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(call: Call<SummaryResponse>, response: Response<SummaryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val summarizerList = body.summarizer?.takeIf { it.isNotEmpty() }

                    if (summarizerList != null) {
                        val chartData = summarizerList.mapNotNull { summary ->
                            val received = summary.summary_Received.safeToFloat()
                            val sent = summary.summary_Sent.safeToFloat()
                            val unknown = summary.summary_Unknown.safeToFloat()

                            if (received == null || sent == null || unknown == null) return@mapNotNull null

                            ChartDataItem(
                                date = formatDate(summary.summary_Created ?: ""),
                                received = received,
                                sent = sent,
                                unknown = unknown
                            )
                        }

                        if (chartData.isNotEmpty()) {
                            viewModel.updateChartData(chartData)
                        } else {
                            showNoDataState()
                        }
                    } else {
                        showNoDataState()
                    }
                } else {
                    showNoDataState()
                }
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                binding.connWaitInternet.visibility = View.GONE
                if (!isConnected()) {
                    Toast.makeText(context, "No internet • Showing last saved data", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Network error • Check connection", Toast.LENGTH_LONG).show()
                }
                Log.d("GraphCache", "Served from cache or failed: ${t.message}")
                showNoDataState()
            }
        })
    }

    fun String.safeToFloat(): Float? = try {
        this.replace(",", "").trim().toFloat()
    } catch (e: Exception) {
        null
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Handle Unix timestamp (seconds since epoch)
            val timestamp = dateString.toLongOrNull()
            if (timestamp != null) {
                // Valid Unix timestamps are > 1000000000 (year 2001) and < 2147483647 (year 2038 for 32-bit)
                // Year-only values like "2026" are 4 digits, not valid timestamps
                if (timestamp > 1000000000L && timestamp < 2147483647L && dateString.length >= 10) {
                    // Seconds timestamp (10 digits for years 2001-2038)
                    val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    outputFormat.format(timestamp * 1000)
                } else if (timestamp > 1000000000000L) {
                    // Milliseconds timestamp (13 digits)
                    val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    outputFormat.format(timestamp)
                } else {
                    // Not a valid timestamp, treat as formatted date string
                    throw NumberFormatException("Not a timestamp")
                }
            } else {
                // Handle formatted date string "yyyy-MM-dd HH:mm:ss"
                throw NumberFormatException("Not a number")
            }
        } catch (e: Exception) {
            // Handle formatted date string "yyyy-MM-dd HH:mm:ss"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date)
            } catch (e2: Exception) {
                // Fallback: try to extract MM-dd from string
                if (dateString.length >= 10) dateString.substring(5, 10) else dateString
            }
        }
    }
}