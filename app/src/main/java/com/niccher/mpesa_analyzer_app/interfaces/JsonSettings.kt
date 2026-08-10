package com.niccher.mpesa_analyzer_app.interfaces

import com.niccher.mpesa_analyzer_app.models.Mod_Settings_Response
import com.niccher.mpesa_analyzer_app.models.Mod_Profile_Response
import com.niccher.mpesa_analyzer_app.models.Mod_Note_Response
import com.niccher.mpesa_analyzer_app.models.Mod_Generic_Response
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface JsonSettings {
    @FormUrlEncoded
    @POST("../api/settings/profile")
    fun getProfile(@Field("varToken") token: String): Call<Mod_Profile_Response>

    @FormUrlEncoded
    @POST("../api/settings/profile/update")
    fun updateProfile(@FieldMap fields: Map<String, String>): Call<Mod_Generic_Response>

    @FormUrlEncoded
    @POST("../api/settings/preferences")
    fun getPreferences(@Field("varToken") token: String): Call<Mod_Settings_Response>

    @FormUrlEncoded
    @POST("../api/settings/preferences/save")
    fun savePreferences(@FieldMap fields: Map<String, String>): Call<Mod_Generic_Response>

    @FormUrlEncoded
    @POST("../api/transactions/notes/save")
    fun saveNote(@FieldMap fields: Map<String, String>): Call<Mod_Generic_Response>

    @FormUrlEncoded
    @POST("../api/process/scan")
    fun scanTrigger(@Field("varToken") token: String): Call<Mod_Generic_Response>

    @FormUrlEncoded
    @POST("../api/process/progress")
    fun scanProgress(@Field("varToken") token: String): Call<Mod_Generic_Response>
}
