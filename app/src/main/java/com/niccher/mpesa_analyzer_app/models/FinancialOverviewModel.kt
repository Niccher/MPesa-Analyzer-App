package com.niccher.mpesa_analyzer_app.models

data class FinancialOverviewResponse(
    val status: Int? = null,
    val time: String? = null,
    val overview: FinancialOverview? = null,
    val message: String? = null
)

data class FinancialOverview(
    val total_transactions: Int = 0,
    val total_senders: Int = 0,
    val finance_senders: Int = 0,
    val category_breakdown: Map<String, Int>? = null,
    val total_amount_sent: Double = 0.0,
    val total_amount_received: Double = 0.0,
    val period_start: String? = null,
    val period_end: String? = null
)
