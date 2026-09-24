package com.niccher.mpesa_analyzer_app.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatMessagePayload(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class ChatRequestPayload(
    @SerializedName("user_id") val userId: String,
    @SerializedName("message") val message: String,
    @SerializedName("history") val history: List<ChatMessagePayload> = emptyList()
)

data class ChatResponsePayload(
    @SerializedName("reply") val reply: String,
    @SerializedName("latency_ms") val latencyMs: Int,
    @SerializedName("tokens_used") val tokensUsed: Int
)

interface ChatApiService {
    @POST("api/v1/chat")
    fun sendChat(@Body request: ChatRequestPayload): Call<ChatResponsePayload>
}
