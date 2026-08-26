package com.niccher.mpesa_analyzer_app.fragments.Home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.models.FinancialOverview

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _overview = MutableLiveData<FinancialOverview?>()
    val overview: LiveData<FinancialOverview?> get() = _overview

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    fun setOverview(data: FinancialOverview?) {
        _overview.value = data
        _isLoading.value = false
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setError(msg: String?) {
        _error.value = msg
        _isLoading.value = false
    }

    data class CategoryInfo(
        val key: String,
        val label: String,
        val colorRes: Int,
        val count: Int = 0,
        val percentage: Float = 0f
    )

    companion object {
        val CATEGORY_META = listOf(
            CategoryInfo("Mobile Money", "Mobile Money", R.color.cat_mobile_money),
            CategoryInfo("Bank", "Bank", R.color.cat_bank),
            CategoryInfo("Fintech", "Fintech", R.color.cat_fintech),
            CategoryInfo("SACCO", "SACCO", R.color.cat_sacco),
            CategoryInfo("Insurance", "Insurance", R.color.cat_insurance),
            CategoryInfo("Payments/Govt", "Payments/Govt", R.color.cat_payments_govt),
            CategoryInfo("Other Finance", "Other Finance", R.color.cat_other_finance),
            CategoryInfo("Non-Finance", "Non-Finance", R.color.cat_non_finance),
            CategoryInfo("Unclassified", "Unclassified", R.color.cat_unclassified),
        )
    }
}
