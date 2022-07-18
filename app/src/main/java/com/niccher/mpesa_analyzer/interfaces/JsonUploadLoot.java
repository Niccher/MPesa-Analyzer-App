package com.niccher.mpesa_analyzer.interfaces;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface JsonUploadLoot {
    @Multipart
    @POST("upload")
    Call<ResponseBody> upload(
            @Part("varToken") RequestBody toke_number,
            @Part("varDevId") RequestBody print_id,
            @Part MultipartBody.Part file
    );
}