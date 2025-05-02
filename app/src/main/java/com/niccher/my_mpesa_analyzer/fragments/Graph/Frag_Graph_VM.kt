package com.niccher.my_mpesa_analyzer.fragments.Graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName

class Frag_Graph_VM : ViewModel() {

    data class SummaryEntry(
        val date: String,
        val received: Float,
        val sent: Float,
        val unknown: Float
    )

    private val _summaryData = MutableLiveData<List<SummaryEntry>>()
    val summaryData: LiveData<List<SummaryEntry>> get() = _summaryData

    init {
        // Load your data (could be from a repo, API, or local DB)
        _summaryData.value = listOf(
            SummaryEntry("2025-05-01", 826f, 6216f, 515f),
            SummaryEntry("2025-04-15", 822f, 6107f, 505f),
            SummaryEntry("2025-04-15", 822f, 6107f, 505f)
        )
    }

    ////////////////
    val summaryData_: LiveData<List<SummaryEntry>> = _summaryData

    private fun loadStaticData() {
        _summaryData.value = listOf(
            SummaryEntry("2025-05-01", 826f, 6216f, 515f),
            SummaryEntry("2025-04-15", 822f, 6107f, 505f),
            SummaryEntry("2025-04-15", 822f, 6107f, 505f)
        )
    }

    fun fetchData() {
        loadStaticData()
    }

}

