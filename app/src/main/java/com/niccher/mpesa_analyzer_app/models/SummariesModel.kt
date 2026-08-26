package com.niccher.mpesa_analyzer_app.models

data class SummariesModel(
    val summary_Loot_Uuid: String,
    val summary_Created: String,
    val summary_Count: Int = 0,
    val summary_Received: Double = 0.0,
    val summary_Sent: Double = 0.0,
    val summary_Unknown: Int = 0,
    val finance_senders: Int = 0,
    val total_amount: Double = 0.0,
    val category_breakdown: Map<String, Int>? = null,
    val direction_breakdown: Map<String, Int>? = null,
    val transaction_type_breakdown: Map<String, Int>? = null,
    val top_counterparties: List<Counterparty>? = null
)
