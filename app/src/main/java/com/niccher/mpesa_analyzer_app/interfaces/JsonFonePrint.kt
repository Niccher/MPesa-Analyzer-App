package com.niccher.mpesa_analyzer_app.interfaces

import com.niccher.mpesa_analyzer_app.models.Mod_Fone_Id
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface JsonFonePrint {

    @FormUrlEncoded
    @POST("device")
    fun createPrint(@FieldMap fields: Map<String, String>): Call<Mod_Fone_Id>
}