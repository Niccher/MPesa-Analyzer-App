package com.niccher.mpesa_analyzer_app.models

data class TransactionsByCategoryResponse(
    val status: Int? = null,
    val time: String? = null,
    val transactions: List<TransactionItem>? = null,
    val total: Int = 0,
    val page: Int = 1,
    val per_page: Int = 50,
    val message: String? = null
)

data class TransactionItem(
    val id: Int = 0,
    val sms_body: String? = null,
    val amount: Double? = null,
    val direction: String? = null,
    val counterparty: String? = null,
    val category: String? = null,
    val sender_name: String? = null,
    val sender_number: String? = null,
    val transaction_type: String? = null,
    val trans_date: String? = null,
    val balance: Double? = null
)
