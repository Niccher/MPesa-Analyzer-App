package com.niccher.mpesa_analyzer.interfaces;


import com.niccher.mpesa_analyzer.models.Mod_Summaries;
import com.niccher.mpesa_analyzer.models.Mod_User_Auth;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface JsonProcesses {

    @FormUrlEncoded
    @POST("get/my_uploads")
    Call<Mod_Summaries> getSummary(@FieldMap Map<String, String> fields);
}