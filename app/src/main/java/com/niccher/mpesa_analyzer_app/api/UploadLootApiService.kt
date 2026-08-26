package com.niccher.mpesa_analyzer_app.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadLootApiService {
    @Multipart
    @POST("api/v1/upload")
    fun upload(
        @Part("varToken") toke_number: RequestBody,
        @Part("varDevId") print_id: RequestBody,
        @Part("varBatch") is_continuation: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}