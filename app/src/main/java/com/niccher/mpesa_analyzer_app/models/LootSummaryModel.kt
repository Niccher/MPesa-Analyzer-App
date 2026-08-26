package com.niccher.mpesa_analyzer_app.models

data class LootSummarizer(
    val status: Int = 0,
    var count_Get_from_MPESA: Int = 0,
    var count_Get_from_KCB: Int = 0,
    val count_Get_from_Mshwari: Int = 0,
    val count_Get_from_NCBA: Int = 0,
    var count_Get_from_IM: Int = 0,
    var count_Get_from_Reversal: Int = 0,
    val count_Get_Bal_MPESA: Int = 0,
    val count_Get_Bal_KCB: Int = 0,
    val count_Get_Bal_Mshwari: Int = 0,
    val count_Loan_Limit: Int = 0,
    val count_Sent_to_MPESA: Int = 0,
    val count_Sent_to_Mshwari: Int = 0,
    val count_Sent_to_LNM: Int = 0,
    val count_Sent_Mini: Int = 0,
    val count_Sent_Cancel: Int = 0,
    val count_Withdraw: Int = 0,
    val count_Error_Failed: Int = 0,
    val count_Error_Pin: Int = 0,
    val count_Error_Less: Int = 0,
    val count_Error_Receiver: Int = 0,
    val count_Error_Receiver_Org: Int = 0,
    val count_Fuliza_Opt_Out: Int = 0,
    val count_Fuliza_Opt_In: Int = 0,
    val count_Fuliza_Limit: Int = 0,
    val count_Fuliza_Loan_Paid: Int = 0,
    val count_Fuliza_Mini_Statement: Int = 0,
    val count_Fuliza_Loan_Taken: Int = 0,
    val count_Similar_Transaction: Int = 0,
    val count_All: Int = 0,
    val count_Unknown: Int = 0,
    val loot_Created: String = "",
    val loot_Uuid: String = "",
    val direction_breakdown: Map<String, Int>? = null,
    val transaction_type_breakdown: Map<String, Int>? = null,
    val category_breakdown: Map<String, Int>? = null,
    val total_amount: Double = 0.0,
    val finance_senders: Int = 0,
    val top_counterparties: List<Counterparty>? = null
)

data class Counterparty(
    val counterparty: String = "",
    val total_amount: Double = 0.0,
    val trans_count: Int = 0
)

data class LootSummaryModel(
    val loot_summarizer: LootSummarizer
)
