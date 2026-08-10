package com.niccher.mpesa_analyzer_app.models

data class Mod_Settings_Response(
    val status: String,
    val settings: Map<String, Any>?
)

data class Mod_Profile_Response(
    val status: String,
    val user: Mod_User_Profile?
)

data class Mod_User_Profile(
    val id: Int,
    val username: String,
    val email: String,
    val created_at: String
)

data class Mod_Note_Response(
    val status: String,
    val note: String
)

data class Mod_Generic_Response(
    val status: String,
    val message: String
)
