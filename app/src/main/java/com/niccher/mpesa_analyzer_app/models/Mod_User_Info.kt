package com.niccher.mpesa_analyzer_app.models

data class Mod_User_Info(
    val status: String,
    val time: String,
    val message: List<UserData>
)

data class UserData(
    val user_Name: String,
    val user_Email: String
)