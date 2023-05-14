package com.niccher.mpesa_analyzer.interfaces;


import com.niccher.mpesa_analyzer.helpers.SummaryResponse;

import java.util.Map;

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
    Call <SummaryResponse> getSummaryCalc(@FieldMap Map<String, String> fields);
}