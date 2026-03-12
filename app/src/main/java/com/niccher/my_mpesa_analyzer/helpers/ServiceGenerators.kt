package com.niccher.mpesa_analyzer.helpers

import android.content.Context
import com.niccher.my_mpesa_analyzer.BuildConfig
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object ServiceGenerators {
    private val kon = Konstants
    private const val CACHE_SIZE = 10L * 1024 * 1024  // 10 MB cache

    fun <S> createService(serviceClass: Class<S>, context: Context): S {
        val retrofit = Retrofit.Builder()
            .baseUrl(kon.LINK_PROCESS)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getUnsafeOkHttpClient(context))
            .build()
        return retrofit.create(serviceClass)
    }

    fun getUnsafeOkHttpClient(context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, CACHE_SIZE)

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val onlineInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Cache-Control", "public, max-age=7200")  // Cache for 1 min online
                .build()
            chain.proceed(request)
        }

        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!isOnline(context)) {
                val cacheControl = CacheControl.Builder()
                    .onlyIfCached()
                    .maxStale(7, TimeUnit.DAYS)  // Allow 7 days stale when offline
                    .build()
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
            }
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineInterceptor)  // Offline first
            .addNetworkInterceptor(onlineInterceptor)  // For responses
            .addInterceptor(logging)
            .connectTimeout(90, TimeUnit.SECONDS)  // Reduced for better UX
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}