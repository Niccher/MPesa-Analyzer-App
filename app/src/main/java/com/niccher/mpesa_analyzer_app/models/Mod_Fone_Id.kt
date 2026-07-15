package com.niccher.mpesa_analyzer_app.models

/**
 * Response from POST /process/device (Upload::device_print).
 * status is an int (1 = success) on the server.
 */
data class Mod_Fone_Id(
    val print_id: String? = null,
    val status: Int = 0,
    val message: String? = null,
    val time: String? = null
)
