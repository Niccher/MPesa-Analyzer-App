package com.niccher.mpesa_analyzer_app.interfaces

import com.niccher.mpesa_analyzer_app.models.FinancialOverviewResponse
import com.niccher.mpesa_analyzer_app.models.SenderProfilesResponse
import com.niccher.mpesa_analyzer_app.models.TransactionsByCategoryResponse
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface JsonFinancial {
    @FormUrlEncoded
    @POST("get/my_financial_overview")
    fun getFinancialOverview(@FieldMap fields: Map<String, String>): Call<FinancialOverviewResponse>

    @FormUrlEncoded
    @POST("get/my_transactions_by_category")
    fun getTransactionsByCategory(@FieldMap fields: Map<String, String>): Call<TransactionsByCategoryResponse>

    @FormUrlEncoded
    @POST("get/my_sender_profiles")
    fun getSenderProfiles(@FieldMap fields: Map<String, String>): Call<SenderProfilesResponse>
}
