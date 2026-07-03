package com.niccher.mpesa_analyzer_app.fragments.Graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Frag_Graph_VM : ViewModel() {

    data class ChartDataItem(
        val date: String,
        val received: Float,
        val sent: Float,
        val unknown: Float
    )

    data class SummaryEntry(
        val date: String,
        val received: Float,
        val sent: Float,
        val unknown: Float
    )

    private val _summaryData = MutableLiveData<List<SummaryEntry>>()
    val summaryData: LiveData<List<SummaryEntry>> get() = _summaryData

    fun updateChartData(data: List<ChartDataItem>) {
        val summaryEntries = data.map { chartDataItem ->
            SummaryEntry(
                date = chartDataItem.date,
                received = chartDataItem.received,
                sent = chartDataItem.sent,
                unknown = chartDataItem.unknown
            )
        }
        _summaryData.value = summaryEntries
    }

    // For initial empty state or testing
    fun fetchData() {
        // This can be empty since we're getting data from API
        _summaryData.value = emptyList()
    }
}