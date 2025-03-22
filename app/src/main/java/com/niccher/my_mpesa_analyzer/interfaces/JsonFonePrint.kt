package com.niccher.my_mpesa_analyzer.interfaces

import com.niccher.my_mpesa_analyzer.models.Mod_Fone_Id
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface JsonFonePrint {

    @FormUrlEncoded
    @POST("device")
    fun createPrint(@FieldMap fields: Map<String, String>): Call<Mod_Fone_Id>
}