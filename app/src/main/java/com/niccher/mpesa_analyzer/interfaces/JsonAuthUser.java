package com.niccher.mpesa_analyzer.interfaces;


import com.niccher.mpesa_analyzer.models.Mod_User_Auth;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface JsonAuthUser {

    /*@FormUrlEncoded
    @POST("register")
    Call<Mod_User_Auth> createRegister(
            @Field("varUsername") String varUsername,
            @Field("varEmail") String varEmail,
            @Field("varPassword") String varPassword
    );;*/

    @FormUrlEncoded
    @POST("register")
    Call<Mod_User_Auth> createRegister(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("login")
    Call<Mod_User_Auth> createLogin(@FieldMap Map<String, String> fields);
}