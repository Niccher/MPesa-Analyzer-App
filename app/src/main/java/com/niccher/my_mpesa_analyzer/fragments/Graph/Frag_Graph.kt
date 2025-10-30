package com.niccher.my_mpesa_analyzer.fragments.Graph

import android.R.attr.data
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.graphics.Color
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.databinding.FragGraphBinding
import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.my_mpesa_analyzer.adapter.Adapter_Frag_History
import com.niccher.my_mpesa_analyzer.fragments.Graph.Frag_Graph_VM.ChartDataItem
import com.niccher.my_mpesa_analyzer.helpers.Prefs
import com.niccher.my_mpesa_analyzer.helpers.SummaryResponse
import com.niccher.my_mpesa_analyzer.interfaces.JsonProcesses
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import com.niccher.my_mpesa_analyzer.models.Mod_Summaries
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.orEmpty

class Frag_Graph : Fragment() {

    private var _binding: FragGraphBinding? = null
    private val binding get() = _binding!!

    private lateinit var connState: TextView
    private lateinit var connWait: ProgressBar

    private lateinit var jsonProcesses: JsonProcesses
    private lateinit var kon: Konstants
    private lateinit var summariesAdapter: Adapter_Frag_History
    private lateinit var pref: Prefs

    private var gson: Gson = GsonBuilder().setLenient().create()
    private var summariesList: ArrayList<Mod_Summaries> = ArrayList()

    private val viewModel: Frag_Graph_VM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        kon = Konstants
        pref = Prefs()

        gson = GsonBuilder()
            .setLenient()
            .create()

        val fragGraph = inflater.inflate(R.layout.frag_graph, container, false)

        connState = fragGraph.findViewById(R.id.conn_no_internet)
        connWait = fragGraph.findViewById(R.id.conn_wait_internet)
        connWait.visibility = View.GONE

        // Initialize binding
        _binding = FragGraphBinding.bind(fragGraph)

        getConnectionState()

        return fragGraph
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up chart observation
        setupChartObservers()
    }

    private fun setupChartObservers() {
        // Observe the LiveData from ViewModel
        viewModel.summaryData.observe(viewLifecycleOwner) { dataList ->
            if (dataList.isNotEmpty()) {
                ChartStacked(dataList)
                // You can also call ChartLines(dataList) here if you want both charts
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getConnectionState() {
        if (isConnected()) {
            connWait.visibility = View.VISIBLE
            connState.visibility = View.GONE
            getSummaries()
        } else {
            connWait.visibility = View.GONE
            isOffline("No internet connection at the moment")
        }
    }

    fun isConnected(): Boolean {
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo?.isConnected == true
    }

    private fun isOffline(msg: String) {
        connState.text = msg
    }

    private fun getSummaries() {
        connWait.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl(kon.LINK_PROCESS)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(ServiceGenerators.getUnsafeOkHttpClient())
            .build()

        jsonProcesses = retrofit.create(JsonProcesses::class.java)

        val parameters = mutableMapOf<String, String>()
        parameters["varUser"] = pref.getPrefsAuth("auth", requireContext())
        parameters["varDev"] = pref.getPrefsAuth("print", requireActivity())

        val call = jsonProcesses.getSummary(parameters)

        call.enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(call: Call<SummaryResponse>, response: Response<SummaryResponse>) {
                connWait.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val summaryResponse = response.body()!!
                    summariesList = ArrayList(summaryResponse.summarizer?.toList().orEmpty())

                    // Transform the data for the chart
                    val chartData = summariesList.map { summary ->
                        ChartDataItem(
                            date = formatDate(summary.summary_Created),
                            received = summary.summary_Received.toFloat(),
                            sent = summary.summary_Sent.toFloat(),
                            unknown = summary.summary_Unknown.toFloat()
                        )
                    }

                    // Update ViewModel with the data from API
                    viewModel.updateChartData(chartData)

                    println("API Data received: ${summariesList.size} items")
                    println("Chart data: $chartData")
                } else {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                connWait.visibility = View.GONE
                isOffline("Unknown error has occurred, please try again later")
                Toast.makeText(context, t.message ?: "Unknown error", Toast.LENGTH_LONG).show()
                Log.e(kon.TAGGED, t.message ?: "Unknown error")
            }
        })
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Convert "2025-10-30 12:17:15" to "Oct 30" or similar shorter format
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            // If parsing fails, return the original string or a shortened version
            dateString.substring(5, 10) // Returns "10-30" from "2025-10-30 12:17:15"
        }
    }

    private fun ChartStacked(dataList: List<Frag_Graph_VM.SummaryEntry>) {
        val entries = dataList.mapIndexed { index, item ->
            BarEntry(index.toFloat(), floatArrayOf(item.received, item.sent, item.unknown))
        }

        val dataSet = BarDataSet(entries, "Transaction Breakdown").apply {
            setColors(Color.GREEN, Color.RED, Color.GRAY)
            stackLabels = arrayOf("Received", "Sent", "Unknown")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.5f
                setValueTextColor(Color.WHITE)
            }

            description.isEnabled = false
            setFitBars(true)
            legend.isEnabled = true
            setScaleEnabled(true)

            // 👇 Enable interactivity
            setPinchZoom(true)
            isDoubleTapToZoomEnabled = true
            isDragEnabled = true
            setVisibleXRangeMaximum(5f)

            // Move view to show latest data if there are many entries
            if (dataSet.entryCount > 5) {
                moveViewToX(dataSet.entryCount.toFloat() - 5f)
            }

            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                form = Legend.LegendForm.SQUARE
                formSize = 12f
                textSize = 12f
                xEntrySpace = 12f
            }

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dataList.map { it.date })
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelCount = dataList.size
                textSize = 10f
            }

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.textSize = 10f

            animateY(1000)
            invalidate()
        }
    }

    // Optional: Line chart implementation
    private fun ChartLines(dataList: List<Frag_Graph_VM.SummaryEntry>) {
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
            color = Color.GREEN
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
        }

        val sentSet = LineDataSet(sentEntries, "Sent").apply {
            color = Color.RED
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
        }

        val unknownSet = LineDataSet(unknownEntries, "Unknown").apply {
            color = Color.GRAY
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
        }

//        binding.lineChart.apply {
//            data = LineData(receivedSet, sentSet, unknownSet)
//
//            xAxis.apply {
//                position = XAxis.XAxisPosition.BOTTOM
//                granularity = 1f
//                valueFormatter = IndexAxisValueFormatter(dates)
//                setDrawGridLines(false)
//            }
//
//            axisLeft.axisMinimum = 0f
//            axisRight.isEnabled = false
//            description.isEnabled = false
//
//            legend.isEnabled = true
//            setTouchEnabled(true)
//            isDragEnabled = true
//            setScaleEnabled(true)
//            setPinchZoom(true)
//
//            animateXY(1000, 1000)
//            invalidate()
//        }
    }
}