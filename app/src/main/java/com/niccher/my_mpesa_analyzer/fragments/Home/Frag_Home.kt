package com.niccher.my_mpesa_analyzer.fragments.Home

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.databinding.FragHomeBinding
import com.niccher.my_mpesa_analyzer.helpers.Prefs
import com.niccher.my_mpesa_analyzer.konstants.Konstants

class Frag_Home : Fragment() {

    private var _binding: FragHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val kon = Konstants
    private val prefs = Prefs()
//    private val init = PayLoade() // Assuming PayLoade is a class
    private val sbsent = StringBuffer()

//    private val jsonProcesses = JsonProcesses() // Assuming JsonProcesses is a class
    private var gson: Gson? = null

    private var prefLootCounter: SharedPreferences? = null
    private var sharedEditor: SharedPreferences.Editor? = null

    lateinit var textGetAndUpload: TextView
    lateinit var textGetLootCount: TextView
    lateinit var lastTime: TextView
    lateinit var permStatus: TextView
    lateinit var permRequest: TextView
    lateinit var progressBar: ProgressBar

    val codeReadSms = 102
    val codeReadStorage = 104
    val codeWriteStorage = 106

    init {
        gson = Gson() // Initialize Gson in the init block
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val solv = inflater.inflate(R.layout.frag_home, container, false) // Replace with your layout

        textGetAndUpload = solv.findViewById(R.id.card_text_upload)
        textGetLootCount = solv.findViewById(R.id.card_text_info_loot)
        lastTime = solv.findViewById(R.id.home_last_upload)
        permStatus = solv.findViewById(R.id.card_text_permission)
        permRequest = solv.findViewById(R.id.card_text_req_permission)
        progressBar = solv.findViewById(R.id.home_upload_state)

        permRequest.visibility = View.GONE
        progressBar.visibility = View.GONE

        checkAndRequestSmsPermission()

        return solv
    }

    override fun onStart() {
        checkAndRequestSmsPermission()
        super.onStart()
    }

    override fun onResume() {
        checkAndRequestSmsPermission()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            permTweak(false)
            requestPermissions(arrayOf(android.Manifest.permission.READ_SMS), codeReadSms)
        } else {
            // Permission already granted
            permTweak(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            codeReadSms -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    permTweak(false)
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT)
                        .show()
                    permTweak(false)
                }
            }
        }
    }

    private fun permTweak(permGranted: Boolean) {
        if (permGranted) {
            Toast.makeText(context,"Granted",Toast.LENGTH_LONG)
            permStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_green))
            permStatus.text = getString(R.string.string_dialog_permission_granted)
            permRequest.visibility = View.GONE
            Toast.makeText(context, "Granted permTweak", Toast.LENGTH_LONG)
        } else {
            Toast.makeText(context,"Not Granted",Toast.LENGTH_LONG)
            permStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_red))
            permStatus.text = getString(R.string.string_dialog_permission_denied)
            permStatus.visibility = View.GONE
            permRequest.visibility = View.VISIBLE
            Toast.makeText(context, "Granted permTweak", Toast.LENGTH_LONG)
        }
    }

}

class PayLoade : AppCompatActivity() {
    private fun readSms() {
//        val uri = Uri.parse("content://sms/inbox")
//        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
//
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
//                val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
//                Log.d("SMS", "From: $address, Body: $body")
//            }
//            cursor.close()
//        }
    }
}