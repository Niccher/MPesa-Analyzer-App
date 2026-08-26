package com.niccher.mpesa_analyzer_app.api

import com.niccher.mpesa_analyzer_app.models.UserAuthModel
import com.niccher.mpesa_analyzer_app.models.UserInfoModel
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Field

interface AuthApiService {

    @FormUrlEncoded
    @POST("api/v1/auth/register")
    fun createRegister(@FieldMap fields: Map<String, String>): Call<UserAuthModel>

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    fun createLogin(@FieldMap fields: Map<String, String>): Call<UserAuthModel>

    @FormUrlEncoded
    @POST("api/v1/auth/verify")
    fun verifyToken(@FieldMap fields: Map<String, String>): Call<UserAuthModel>

    @FormUrlEncoded
    @POST("api/v1/settings/delete-account")
    fun deleteAccount(
        @Field("varToken") token: String?
    ): Call<com.niccher.mpesa_analyzer_app.models.DeleteAccountModel>

    @FormUrlEncoded
    @POST("api/v1/settings/delete-data")
    fun deleteData(
        @Field("varToken") token: String?
    ): Call<com.niccher.mpesa_analyzer_app.models.DeleteAccountModel>

    @FormUrlEncoded
    @POST("api/v1/settings/profile")
    fun getUserInfo(@Field("varUser") token: String): Call<UserInfoModel>
}