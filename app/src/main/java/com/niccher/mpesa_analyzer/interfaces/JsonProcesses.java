package com.niccher.mpesa_analyzer.interfaces;


import com.niccher.mpesa_analyzer.helpers.SummaryResponse;
import com.niccher.mpesa_analyzer.models.Mod_Loot_Summary;
import com.niccher.mpesa_analyzer.models.Mod_My_Loot_Count;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface JsonProcesses {

    @FormUrlEncoded
    @POST("get/my_uploads")
    Call <SummaryResponse> getSummary(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("get/my_summary_calculations")
    Call <Mod_Loot_Summary> getSummaryCalc(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("get/my_uploads_count")
    Call <Mod_My_Loot_Count> getLootCount(@FieldMap Map<String, String> fields);

    /*
    @FormUrlEncoded
    @POST("device")
    Call<Mod_Fone_Id> createPrint(@FieldMap Map<String, String> fields);
     */
}