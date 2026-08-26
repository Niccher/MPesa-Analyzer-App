package com.niccher.mpesa_analyzer_app.models

data class GraphsModel(
    val summary_Loot_Uuid: String,
    val summary_Created: String,
    val summary_Count: Int = 0,
    val summary_Received: Int = 0,
    val summary_Sent: Int = 0,
    val summary_Unknown: Int = 0
)
