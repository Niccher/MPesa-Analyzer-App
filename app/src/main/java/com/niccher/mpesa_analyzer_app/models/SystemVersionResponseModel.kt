package com.niccher.mpesa_analyzer_app.models

import com.google.gson.annotations.SerializedName

data class SystemVersionResponseModel(
    @SerializedName("version") val version: String,
    @SerializedName("github_url") val githubUrl: String,
    @SerializedName("changelog") val changelog: List<String>
)
