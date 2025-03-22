package com.niccher.my_mpesa_analyzer.interfaces

import com.niccher.my_mpesa_analyzer.helpers.SummaryResponse
import com.niccher.my_mpesa_analyzer.models.Mod_My_Loot_Count
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface JsonProcesses {

    @FormUrlEncoded
    @POST("get/my_uploads")
    fun getSummary(@FieldMap fields: Map<String, String>): Call<SummaryResponse>

//    @FormUrlEncoded
//    @POST("get/my_summary_calculations")
//    fun getSummaryCalc(@FieldMap fields: Map<String, String>): Call<ModLootSummary>
//
    @FormUrlEncoded
    @POST("get/my_uploads_count")
    fun getLootCount(@FieldMap fields: Map<String, String>): Call<Mod_My_Loot_Count>
//
//    @FormUrlEncoded
//    @POST("get/my_uploads_category_count")
//    fun getLootCountCategories(@FieldMap fields: Map<String, String>): Call<SummaryLootResponse>
//
//    @FormUrlEncoded
//    @POST("get/list_all_sms_in_category")
//    fun getAllSmsInCategory(@FieldMap fields: Map<String, String>): Call<SummaryLootResponse>
//
//    @FormUrlEncoded
//    @POST("set/delete_loot_by_uuid")
//    fun getLootDelete(@FieldMap fields: Map<String, String>): Call<ModLootDelete>

}