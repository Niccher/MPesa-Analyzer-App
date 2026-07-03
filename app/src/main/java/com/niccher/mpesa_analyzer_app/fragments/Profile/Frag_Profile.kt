package com.niccher.mpesa_analyzer_app.fragments.Profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.konstants.Konstants

class Frag_Profile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.frag_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load user data from SharedPreferences (same prefs used by auth)
        val prefs = requireContext().getSharedPreferences(
            Konstants.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE
        )

        val username = prefs.getString("userName", "User")?.replaceFirstChar { it.uppercase() } ?: "User"
        val email = prefs.getString("userEmail", "") ?: ""
        val memberSince = prefs.getString("time", "") ?: ""

        view.findViewById<TextView>(R.id.tv_username).text = username
        if (email.isNotBlank() && email != "nullable") {
            view.findViewById<TextView>(R.id.tv_email).text = email
        }
        if (memberSince.isNotBlank() && memberSince != "nullable") {
            view.findViewById<TextView>(R.id.tv_member_since).text = "Logged in at $memberSince"
        }
    }
}
