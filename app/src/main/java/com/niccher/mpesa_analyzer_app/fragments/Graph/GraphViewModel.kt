package com.niccher.mpesa_analyzer_app.fragments.Graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.niccher.mpesa_analyzer_app.R

class GraphViewModel : ViewModel() {

    data class CategoryEntry(
        val label: String,
        val count: Float,
        val colorRes: Int
    )

    private val _categoryData = MutableLiveData<List<CategoryEntry>>()
    val categoryData: LiveData<List<CategoryEntry>> get() = _categoryData

    private val _totalAnalyzed = MutableLiveData<String>("0")
    val totalAnalyzed: LiveData<String> get() = _totalAnalyzed

    private val _categoriesDetected = MutableLiveData<String>("0")
    val categoriesDetected: LiveData<String> get() = _categoriesDetected

    private val _financeSenders = MutableLiveData<String>("0")
    val financeSenders: LiveData<String> get() = _financeSenders

    private val _period = MutableLiveData<String>("--")
    val period: LiveData<String> get() = _period

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun updateKpiData(
        entries: List<CategoryEntry>,
        analyzed: String,
        categories: String,
        senders: String,
        periodStr: String
    ) {
        _categoryData.value = entries
        _totalAnalyzed.value = analyzed
        _categoriesDetected.value = categories
        _financeSenders.value = senders
        _period.value = periodStr
        _isLoading.value = false
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    companion object {
        val CATEGORY_META = listOf(
            CategoryMeta("Mobile Money", R.color.cat_mobile_money),
            CategoryMeta("Bank", R.color.cat_bank),
            CategoryMeta("Fintech", R.color.cat_fintech),
            CategoryMeta("SACCO", R.color.cat_sacco),
            CategoryMeta("Insurance", R.color.cat_insurance),
            CategoryMeta("Payments/Govt", R.color.cat_payments_govt),
            CategoryMeta("Other Finance", R.color.cat_other_finance),
            CategoryMeta("Non-Finance", R.color.cat_non_finance),
            CategoryMeta("Unclassified", R.color.cat_unclassified),
        )
    }

    data class CategoryMeta(val label: String, val colorRes: Int)
}
