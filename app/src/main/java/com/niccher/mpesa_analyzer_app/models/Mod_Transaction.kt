package com.niccher.mpesa_analyzer_app.models

data class Mod_Transaction(
    val id: String,
    val date: Long,
    val address: String,
    val body: String,
    var type: String = "Unknown",
    var amount: Double = 0.0,
    var reference: String = "",
    var name: String = ""
)
