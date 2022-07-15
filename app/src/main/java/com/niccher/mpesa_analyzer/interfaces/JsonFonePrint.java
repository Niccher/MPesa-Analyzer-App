package com.niccher.mpesa_analyzer.interfaces;

import com.niccher.mpesa_analyzer.models.Mod_Fone_Id;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface JsonFonePrint {

    @FormUrlEncoded
    @POST("device")
    Call<Mod_Fone_Id> createPrint(@FieldMap Map<String, String> fields);
}