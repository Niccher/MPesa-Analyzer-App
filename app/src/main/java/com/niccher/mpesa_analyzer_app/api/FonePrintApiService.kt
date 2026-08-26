package com.niccher.mpesa_analyzer_app.api

import com.niccher.mpesa_analyzer_app.models.FoneIdModel
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FonePrintApiService {

    @FormUrlEncoded
    @POST("api/v1/device")
    fun createPrint(@FieldMap fields: Map<String, String>): Call<FoneIdModel>
}