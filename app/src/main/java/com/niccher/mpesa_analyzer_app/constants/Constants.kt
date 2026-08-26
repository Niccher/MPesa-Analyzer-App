package com.niccher.mpesa_analyzer_app.constants

import com.niccher.mpesa_analyzer_app.BuildConfig

object Constants {

    const val TAGGED = "My_MPesa_Analyzer"

    const val SPLASH_TIME = 1500


    const val SHARED_AUTH_LOGIN = "auth_login"
    const val SHARED_AUTH_REGISTER = "auth_register"
    const val SHARED_DEVICE_ID = "pref_device_id"
    const val SHARED_LOOT_COUNT = "pref_loot_count"

    const val STRING_READ_WRITE_BLOCK = 1024

    const val STRING_ALGO_ENCRYPTOR = "AES/CBC/PKCS5Padding"
    const val STRING_ALGO = "AES"
    const val STRING_CHARACTER_ENCODING = "UTF-8"

    // Read from BuildConfig so keys can be changed via build.gradle.kts
    val STRING_KEY: String get() = BuildConfig.MPESA_CRYPT_KEY
    val STRING_KEY_SPEC: String get() = BuildConfig.MPESA_CRYPT_IV

    const val STRING_PLAIN_FILE = "enc_plain_"
    const val STRING_ENC_AES_FILES = "enc_aes_"
    const val STRING_ENC_GZIP_AES_FILES = "enc_gzip_aes_"
    const val STRING_ENC_GZIP_PLAIN_FILES = "enc_gzip_plain_"

    const val SHARED_LAST_TIME = "last_upload_time"
    const val SHARED_LAST_SMS_ID = "last_upload_sms_id"

}