package com.niccher.mpesa_analyzer_app.fragments.Profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.niccher.mpesa_analyzer.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.models.SummaryResponse
import com.niccher.mpesa_analyzer_app.api.AuthApiService
import com.niccher.mpesa_analyzer_app.api.ProcessesApiService
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.models.UserInfoModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var kon: Constants
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvSyncCount: TextView
    private lateinit var tvTotalUploads: TextView
    private lateinit var tvLastSync: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kon = Constants
        tvUsername = view.findViewById(R.id.tv_username)
        tvEmail = view.findViewById(R.id.tv_email)
        tvMemberSince = view.findViewById(R.id.tv_member_since_info)
        tvSyncCount = view.findViewById(R.id.tv_sync_count)
        tvTotalUploads = view.findViewById(R.id.tv_total_uploads)
        tvLastSync = view.findViewById(R.id.tv_last_sync)

        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Load user data from SharedPreferences
        val prefs = requireContext().getSharedPreferences(
            Constants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE
        )

        val userId = prefs.getString("userid", "") ?: ""
        val username = prefs.getString("user_name", "User")?.replaceFirstChar { it.uppercase() } ?: "User"
        val email = prefs.getString("user_email", "") ?: ""
        val memberSince = prefs.getString("time", "") ?: ""

        // Set local data immediately
        tvUsername.text = username
        if (email.isNotBlank() && email != "nullable") {
            tvEmail.text = email
        }
        if (memberSince.isNotBlank() && memberSince != "nullable") {
            tvMemberSince.text = "Member since: $memberSince"
        }

        // Load sync stats from local prefs
        val lootPrefs = requireContext().getSharedPreferences(Constants.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)
        val syncCount = lootPrefs.getInt("loot_count", 0)
        tvSyncCount.text = "$syncCount Times"

        // Fetch real profile data from server if userId exists
        if (userId.isNotBlank()) {
            fetchUserInfoFromServer(userId)
            fetchUploadStatsFromServer(userId, prefs.getString("print", "") ?: "")
        }
    }

    private fun fetchUserInfoFromServer(userId: String) {
        val jsonAuthUser = ServiceGenerator.createService(AuthApiService::class.java, requireContext())
        jsonAuthUser.getUserInfo(userId).enqueue(object : Callback<UserInfoModel> {
            override fun onResponse(call: Call<UserInfoModel>, response: Response<UserInfoModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!.message
                    if (userData.isNotEmpty()) {
                        val user = userData[0]
                        // Update with fresh data from server
                        tvUsername.text = user.user_Name.replaceFirstChar { it.uppercase() }
                        tvEmail.text = user.user_Email
                        // Save to local prefs for offline access
                        saveUserDataToPrefs(user.user_Name, user.user_Email)
                    }
                }
            }

            override fun onFailure(call: Call<UserInfoModel>, t: Throwable) {
                // Silently fail - we already have local data
            }
        })
    }

    private fun fetchUploadStatsFromServer(userId: String, deviceId: String) {
        val jsonProcesses = ServiceGenerator.createService(ProcessesApiService::class.java, requireContext())
        val params = mapOf(
            "varUser" to userId,
            "varDev" to deviceId
        )
        val call = jsonProcesses.getSummary(params)
        call.enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(call: Call<SummaryResponse>, response: Response<SummaryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val summaries = response.body()!!.summarizer?.toList() ?: emptyList()
                    if (summaries.isNotEmpty()) {
                        // Update total uploads count
                        tvTotalUploads.text = summaries.size.toString()

                        // Update last sync time from most recent upload
                        val latest = summaries.maxByOrNull { it.summary_Created }
                        latest?.let {
                            val lastSync = formatDate(it.summary_Created)
                            tvLastSync.text = lastSync
                        }
                    } else {
                        tvTotalUploads.text = "0"
                        tvLastSync.text = "Never"
                    }
                }
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                // Use local data
                val lootPrefs = requireContext().getSharedPreferences(Constants.SHARED_LOOT_COUNT, Context.MODE_PRIVATE)
                val syncCount = lootPrefs.getInt("loot_count", 0)
                tvTotalUploads.text = syncCount.toString()
                tvLastSync.text = "Unknown"
            }
        })
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun saveUserDataToPrefs(username: String, email: String) {
        val prefs = requireContext().getSharedPreferences(
            Constants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE
        ).edit().apply {
            putString("user_name", username)
            putString("user_email", email)
        }
    }
}