package com.niccher.mpesa_analyzer_app.models

data class SenderProfilesResponse(
    val status: Int? = null,
    val time: String? = null,
    val profiles: List<SenderProfile>? = null,
    val message: String? = null
)

data class SenderProfile(
    val number: String? = null,
    val name: String? = null,
    val category: String? = null,
    val is_finance: Boolean = false,
    val confidence: Double = 0.0,
    val transaction_count: Int = 0,
    val total_amount: Double = 0.0
)
