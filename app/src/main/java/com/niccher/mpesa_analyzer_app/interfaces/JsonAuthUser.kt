package com.niccher.mpesa_analyzer_app.interfaces

import com.niccher.mpesa_analyzer_app.models.Mod_User_Auth
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Field

interface JsonAuthUser {

    @FormUrlEncoded
    @POST("register")
    fun createRegister(@FieldMap fields: Map<String, String>): Call<Mod_User_Auth>

    @FormUrlEncoded
    @POST("login")
//    fun createLogin(@FieldMap fields: String): Call<Mod_User_Auth>
    fun createLogin(@FieldMap fields: Map<String, String>): Call<Mod_User_Auth>

    @FormUrlEncoded
    @POST("verify_token")
    fun verifyToken(@FieldMap fields: Map<String, String>): Call<Mod_User_Auth>

    @FormUrlEncoded
    @POST("delete_account")
    fun delete_account(
        @Field("varToken") token: String?
    ): Call<com.niccher.mpesa_analyzer_app.models.Mod_Delete_Account>

    @FormUrlEncoded
    @POST("delete_data")
    fun delete_data(
        @Field("varToken") token: String?
    ): Call<com.niccher.mpesa_analyzer_app.models.Mod_Delete_Account>
}