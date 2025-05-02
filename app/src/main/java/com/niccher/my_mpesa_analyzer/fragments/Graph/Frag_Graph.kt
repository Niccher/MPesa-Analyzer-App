package com.niccher.my_mpesa_analyzer.fragments.Graph

import android.R.attr.data
import android.os.Bundle
import android.view.LayoutInflater
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
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

class Frag_Graph : Fragment() {

    private var _binding: FragGraphBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: Frag_Graph_VM by viewModels()

    data class SummaryEntry(
        val date: String,
        val received: Float,
        val sent: Float,
        val unknown: Float
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.frag_graph, container, false)
        _binding = FragGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ChartStacked()
        ChartLines()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun ChartStacked(){
        viewModel.summaryData.observe(viewLifecycleOwner) { dataList ->
            val entries = dataList.mapIndexed { index, item ->
                BarEntry(index.toFloat(), floatArrayOf(item.received, item.sent, item.unknown))
            }

            val dataSet = BarDataSet(entries, "Message Breakdown").apply {
                setColors(Color.GREEN, Color.BLUE, Color.GRAY)
                stackLabels = arrayOf("Received", "Sent", "Unknown")
            }

            binding.barChart.apply {
                data = BarData(dataSet).apply { barWidth = 0.5f }
                description.isEnabled = false
                setFitBars(true)
                legend.isEnabled = true
                setScaleEnabled(false)

                // 👇 Enable interactivity
                setPinchZoom(true)            // Zoom both X and Y
                isDoubleTapToZoomEnabled = true
                setScaleEnabled(true)         // Allow scaling
                isDragEnabled = true          // Allow dragging
                setVisibleXRangeMaximum(5f)   // Show 5 bars initially
                moveViewToX(dataSet.entryCount.toFloat()) // Scroll to end

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
                }

                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f

                invalidate()
            }
        }
    }

    private fun ChartLines(){
        viewModel.summaryData.observe(viewLifecycleOwner) { summaryList ->
            val receivedEntries = mutableListOf<Entry>()
            val sentEntries = mutableListOf<Entry>()
            val unknownEntries = mutableListOf<Entry>()
            val dates = mutableListOf<String>()

            summaryList.forEachIndexed { index, item ->
                receivedEntries.add(Entry(index.toFloat(), item.received))
                sentEntries.add(Entry(index.toFloat(), item.sent))
                unknownEntries.add(Entry(index.toFloat(), item.unknown))
                dates.add(item.date)
            }

            val receivedSet = LineDataSet(receivedEntries, "Received").apply {
                color = Color.GREEN
                circleRadius = 4f
                setDrawValues(false)
            }

            val sentSet = LineDataSet(sentEntries, "Sent").apply {
                color = Color.BLUE
                circleRadius = 4f
                setDrawValues(false)
            }

            val unknownSet = LineDataSet(unknownEntries, "Unknown").apply {
                color = Color.GRAY
                circleRadius = 4f
                setDrawValues(false)
            }

            binding.lineChart.apply {
                data = LineData(receivedSet, sentSet, unknownSet)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(dates)
                }

                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false
                description.isEnabled = false

                legend.isEnabled = true
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                invalidate()
            }
        }
        viewModel.fetchData()
    }
}