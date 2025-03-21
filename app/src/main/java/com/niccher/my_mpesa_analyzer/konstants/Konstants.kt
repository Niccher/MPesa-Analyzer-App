package com.niccher.my_mpesa_analyzer.konstants

object Konstants {

    const val TAGGED = "MPesa_Analyzer"

    const val SPLASH_TIME = 1500

    private const val STRING_IP = "https://mympesa.chegecache.co.ke"
    const val UPLOAD_FILE_URL = "$STRING_IP/process/"
    const val UPLOAD_AUTH_URL = "$STRING_IP/auth/"
    const val LINK_PROCESS = "$STRING_IP/process/"

    const val SHARED_AUTH_LOGIN = "auth_login"
    const val SHARED_AUTH_REGISTER = "auth_register"
    const val SHARED_DEVICE_ID = "pref_device_id"
    const val SHARED_LOOT_COUNT = "pref_loot_count"

    const val STRING_READ_WRITE_BLOCK = 1024

    const val STRING_ALGO_ENCRYPTOR = "AES/CBC/PKCS5Padding"
    const val STRING_ALGO = "AES"
    const val STRING_CHARACTER_ENCODING = "UTF-8"

    const val STRING_KEY = "a:r2yt>N3_\\Py,f="
    const val STRING_KEY_SPEC = "[M[@_w[F4a>yQsJW"

    const val STRING_PLAIN_FILE = "enc_plain_"
    const val STRING_ENC_AES_FILES = "enc_aes_"
    const val STRING_ENC_GZIP_AES_FILES = "enc_gzip_aes_"
    const val STRING_ENC_GZIP_PLAIN_FILES = "enc_gzip_plain_"

    const val SHARED_LAST_TIME = "last_upload_time"

}