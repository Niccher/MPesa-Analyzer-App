package com.niccher.mpesa_analyzer_app.api

import com.niccher.mpesa_analyzer_app.models.SettingsResponseModel
import com.niccher.mpesa_analyzer_app.models.ProfileResponseModel
import com.niccher.mpesa_analyzer_app.models.NoteResponseModel
import com.niccher.mpesa_analyzer_app.models.GenericResponseModel
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SettingsApiService {
    @FormUrlEncoded
    @POST("api/v1/settings/profile")
    fun getProfile(@Field("varToken") token: String): Call<ProfileResponseModel>

    @FormUrlEncoded
    @POST("api/v1/settings/profile/update")
    fun updateProfile(@FieldMap fields: Map<String, String>): Call<GenericResponseModel>

    @FormUrlEncoded
    @POST("api/v1/settings/preferences")
    fun getPreferences(@Field("varToken") token: String): Call<SettingsResponseModel>

    @FormUrlEncoded
    @POST("api/v1/settings/preferences/save")
    fun savePreferences(@FieldMap fields: Map<String, String>): Call<GenericResponseModel>

    @FormUrlEncoded
    @POST("api/v1/notes/save")
    fun saveNote(@FieldMap fields: Map<String, String>): Call<GenericResponseModel>

    @FormUrlEncoded
    @POST("api/v1/process/scan")
    fun scanTrigger(@Field("varToken") token: String): Call<GenericResponseModel>

    @FormUrlEncoded
    @POST("api/v1/process/progress")
    fun scanProgress(@Field("varToken") token: String): Call<GenericResponseModel>

    @retrofit2.http.GET("api/v1/system/version")
    fun getSystemVersion(): Call<com.niccher.mpesa_analyzer_app.models.SystemVersionResponseModel>
}
