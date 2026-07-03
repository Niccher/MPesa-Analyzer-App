package com.niccher.mpesa_analyzer_app.models

data class LootSummarizer(
    val status: Int,
    var count_Get_from_MPESA: String,
    var count_Get_from_KCB: String,
    val count_Get_from_Mshwari: String,
    val count_Get_from_NCBA: String,
    var count_Get_from_IM: String,
    var count_Get_from_Reversal: String,
    val count_Get_Bal_MPESA: String,
    val count_Get_Bal_KCB: String,
    val count_Get_Bal_Mshwari: String,
    val count_Loan_Limit: String,
    val count_Sent_to_MPESA: String,
    val count_Sent_to_Mshwari: String,
    val count_Sent_to_LNM: String,
    val count_Sent_Mini: String,
    val count_Sent_Cancel: String,
    val count_Withdraw: String,
    val count_Error_Failed: String,
    val count_Error_Pin: String,
    val count_Error_Less: String,
    val count_Error_Receiver: String,
    val count_Error_Receiver_Org: String,
    val count_Fuliza_Opt_Out: String,
    val count_Fuliza_Opt_In: String,
    val count_Fuliza_Limit: String,
    val count_Fuliza_Loan_Paid: String,
    val count_Fuliza_Mini_Statement: String,
    val count_Fuliza_Loan_Taken: String,
    val count_Similar_Transaction: String,
    val count_All: String,
    val count_Unknown: String,
    val loot_Created: String,
    val loot_Uuid: String
)

data class Mod_Loot_Summary(
    val loot_summarizer: LootSummarizer
)