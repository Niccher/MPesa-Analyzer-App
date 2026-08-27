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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.databinding.FragGraphBinding
import com.niccher.mpesa_analyzer_app.fragments.Graph.GraphViewModel.CategoryEntry
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.api.FinancialApiService
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.models.FinancialOverviewResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class GraphFragment : Fragment() {

    private var _binding: FragGraphBinding? = null
    private val binding get() = _binding!!

    private lateinit var kon: Constants
    private lateinit var pref: Prefs

    private val viewModel: GraphViewModel by viewModels()

    private var currentChartType: ChartType = ChartType.BAR

    enum class ChartType {
        BAR, PIE
    }

    private val categoryColors = intArrayOf(
        Color.parseColor("#4CAF50"),
        Color.parseColor("#2196F3"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#00BCD4"),
        Color.parseColor("#607D8B"),
        Color.parseColor("#9E9E9E"),
        Color.parseColor("#78909C"),
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        kon = Constants
        pref = Prefs()
        _binding = FragGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
        fetchData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        binding.barChart.visibility = View.GONE
        binding.pieChart.visibility = View.GONE
        binding.connWaitInternet.visibility = View.GONE
        binding.connNoInternet.visibility = View.GONE

        binding.chartBarTab.setOnClickListener { switchToBar() }
        binding.chartPieTab.setOnClickListener { switchToPie() }
        updateToggleVisuals()
    }

    private fun switchToBar() {
        if (currentChartType == ChartType.BAR) return
        currentChartType = ChartType.BAR
        updateToggleVisuals()
        viewModel.categoryData.value?.let { renderChart(it) }
    }

    private fun switchToPie() {
        if (currentChartType == ChartType.PIE) return
        currentChartType = ChartType.PIE
        updateToggleVisuals()
        viewModel.categoryData.value?.let { renderChart(it) }
    }

    private fun updateToggleVisuals() {
        val isBar = currentChartType == ChartType.BAR
        binding.chartBarTab.setBackgroundResource(if (isBar) R.drawable.bg_toggle_left else 0)
        binding.chartPieTab.setBackgroundResource(if (isBar) 0 else R.drawable.bg_toggle_right)
        val active = Color.WHITE
        val inactive = Color.parseColor("#66FFFFFF")
        binding.iconBar.setColorFilter(active)
        binding.txtBar.setTextColor(active)
        binding.iconPie.setColorFilter(if (isBar) inactive else active)
        binding.txtPie.setTextColor(if (isBar) inactive else active)
    }

    private fun setupObservers() {
        viewModel.categoryData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.connWaitInternet.visibility = View.GONE
                binding.connNoInternet.visibility = View.GONE
                renderChart(data)
            } else {
                showEmptyState()
            }
        }

        viewModel.totalAnalyzed.observe(viewLifecycleOwner) { v ->
            binding.kpiTotalAnalyzed.text = v
        }

        viewModel.categoriesDetected.observe(viewLifecycleOwner) { v ->
            binding.kpiCategories.text = v
        }

        viewModel.financeSenders.observe(viewLifecycleOwner) { v ->
            binding.kpiFinSenders.text = v
        }

        viewModel.period.observe(viewLifecycleOwner) { v ->
            binding.kpiPeriod.text = v
        }
    }

    private fun renderChart(categoryList: List<CategoryEntry>) {
        when (currentChartType) {
            ChartType.BAR -> showBarChart(categoryList)
            ChartType.PIE -> showPieChart(categoryList)
        }
    }

    private fun showBarChart(categoryList: List<CategoryEntry>) {
        binding.pieChart.visibility = View.GONE
        binding.barChart.visibility = View.VISIBLE

        val labels = categoryList.map { it.label }
        val entries = categoryList.mapIndexed { i, e -> BarEntry(i.toFloat(), e.count) }

        val colors = categoryList.mapIndexed { i, e ->
            categoryColors[GraphViewModel.CATEGORY_META.indexOfFirst { it.label == e.label }.coerceAtLeast(0) % categoryColors.size]
        }

        val dataSet = BarDataSet(entries, "Categories").apply {
            setColors(colors)
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            setDrawValues(true)
        }

        buildLegend(categoryList)

        binding.barChart.apply {
            clear()
            this.data = BarData(dataSet).apply { barWidth = 0.6f }
            setExtraOffsets(8f, 8f, 8f, 12f)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(false)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 9f
                setAvoidFirstLastClipping(true)
                labelRotationAngle = -35f
            }

            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
                setDrawGridLines(true)
                textSize = 9f
                setDrawLabels(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(v: Float) = v.toInt().toString()
                }
            }
            axisRight.isEnabled = false

            animateY(600)
            invalidate()
        }
    }

    private fun showPieChart(categoryList: List<CategoryEntry>) {
        binding.barChart.visibility = View.GONE
        binding.pieChart.visibility = View.VISIBLE

        val entries = categoryList.filter { it.count > 0 }.map { PieEntry(it.count.toFloat(), it.label) }

        val colors = entries.map { e ->
            val idx = GraphViewModel.CATEGORY_META.indexOfFirst { it.label == e.label }.coerceAtLeast(0)
            categoryColors[idx % categoryColors.size]
        }

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            valueLinePart1Length = 0.3f
            valueLinePart2Length = 0.5f
            valueLineColor = Color.LTGRAY
            setDrawValues(true)
            sliceSpace = 1f
            selectionShift = 4f
        }

        buildLegend(categoryList)

        binding.pieChart.apply {
            clear()
            this.data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = false
            setDrawEntryLabels(true)
            setEntryLabelTextSize(9f)
            setEntryLabelColor(Color.DKGRAY)
            legend.isEnabled = false
            setExtraOffsets(4f, 4f, 4f, 4f)
            animateY(600)
            invalidate()
        }
    }

    private fun buildLegend(categoryList: List<CategoryEntry>) {
        binding.customLegend.removeAllViews()
        val dotSize = 8
        for (cat in categoryList) {
            val idx = GraphViewModel.CATEGORY_META.indexOfFirst { it.label == cat.label }.coerceAtLeast(0)
            val color = categoryColors[idx % categoryColors.size]

            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    setMargins(0, 0, 3, 0)
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(color)
                }
            }
            binding.customLegend.addView(dot)

            val label = TextView(requireContext()).apply {
                text = "${cat.label} (${cat.count.toInt()})"
                setTextColor(Color.parseColor("#555555"))
                textSize = 9f
                setTypeface(null, android.graphics.Typeface.NORMAL)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 10, 0) }
            }
            binding.customLegend.addView(label)
        }
    }

    private fun showEmptyState() {
        binding.barChart.visibility = View.GONE
        binding.pieChart.visibility = View.GONE
        binding.connNoInternet.visibility = View.VISIBLE
        binding.connNoInternet.text = getString(R.string.no_data_available)
    }

    private fun fetchData() {
        binding.connWaitInternet.visibility = View.VISIBLE
        binding.barChart.visibility = View.GONE
        binding.pieChart.visibility = View.GONE
        binding.connNoInternet.visibility = View.GONE

        val jsonFinancial = ServiceGenerator.createService(FinancialApiService::class.java, requireContext())

        val params = mapOf(
            "varUser" to pref.getPrefsAuth("auth", requireContext()),
            "varDev" to pref.getPrefsAuth("print", requireActivity())
        )

        jsonFinancial.getFinancialOverview(params).enqueue(object : Callback<FinancialOverviewResponse> {
            override fun onResponse(call: Call<FinancialOverviewResponse>, response: Response<FinancialOverviewResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == 1 && body.overview != null) {
                        val overview = body.overview
                        val breakdown = overview.category_breakdown ?: emptyMap()
                        val total = overview.total_transactions

                        val catsCount = breakdown.count { (_, v) -> v > 0 }.toString()
                        val senders = overview.finance_senders.toString()
                        val periodStr = buildPeriodString(overview.period_start, overview.period_end)

                        if (total > 0 && breakdown.isNotEmpty()) {
                            val entries = GraphViewModel.CATEGORY_META.mapNotNull { meta ->
                                val count = breakdown[meta.label] ?: return@mapNotNull null
                                if (count <= 0) return@mapNotNull null
                                CategoryEntry(meta.label, count.toFloat(), meta.colorRes)
                            }
                            viewModel.updateKpiData(entries, total.toString(), catsCount, senders, periodStr)
                        } else {
                            showEmptyState()
                        }
                    } else {
                        showEmptyState()
                    }
                } else {
                    showEmptyState()
                }
            }

            override fun onFailure(call: Call<FinancialOverviewResponse>, t: Throwable) {
                binding.connWaitInternet.visibility = View.GONE
                if (!isConnected()) {
                    binding.connNoInternet.visibility = View.VISIBLE
                    binding.connNoInternet.text = getString(R.string.str_no_internet_connection)
                } else {
                    showEmptyState()
                }
                Log.e(kon.TAGGED, "Graph fetch failed: ${t.message}")
            }
        })
    }

    private fun buildPeriodString(start: String?, end: String?): String {
        if (start == null || end == null) return "--"
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val s = fmt.parse(start.take(10))
            val e = fmt.parse(end.take(10))
            val out = SimpleDateFormat("MMM dd", Locale.US)
            "${out.format(s)} - ${out.format(e)}"
        } catch (x: Exception) {
            "${start.take(10)} - ${end.take(10)}"
        }
    }

    private val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    private fun isConnected(): Boolean {
        val cm = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }
}
