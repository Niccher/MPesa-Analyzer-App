package com.niccher.mpesa_analyzer_app.fragments.Info

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.niccher.mpesa_analyzer_app.BuildConfig
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.api.SettingsApiService
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.models.SystemVersionResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_app_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtAppVersion = view.findViewById<TextView>(R.id.txtAppVersion)
        val localVersion = BuildConfig.VERSION_NAME

        txtAppVersion.text = "Local Version: v$localVersion"

        // Asynchronously check server version
        try {
            val apiService = ServiceGenerator.createService(SettingsApiService::class.java, requireContext())
            apiService.getSystemVersion().enqueue(object : Callback<SystemVersionResponseModel> {
                override fun onResponse(
                    call: Call<SystemVersionResponseModel>,
                    response: Response<SystemVersionResponseModel>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val serverVersion = response.body()!!.version
                        if (serverVersion != localVersion) {
                            txtAppVersion.text = "Local Version: v$localVersion (Update available: v$serverVersion)"
                            txtAppVersion.setTextColor(Color.RED)
                        } else {
                            txtAppVersion.text = "Version: v$localVersion (Latest)"
                        }
                    }
                }

                override fun onFailure(call: Call<SystemVersionResponseModel>, t: Throwable) {
                    // Fail silently, keeping local version displayed
                }
            })
        } catch (e: Exception) {
            // Keep local version displayed
        }
    }
}
