package com.niccher.my_mpesa_analyzer.fragments.History

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.adapter.Adapter_Frag_History
import com.niccher.my_mpesa_analyzer.databinding.FragHistoryBinding
import com.niccher.my_mpesa_analyzer.helpers.Prefs
import com.niccher.my_mpesa_analyzer.helpers.SummaryResponse
import com.niccher.my_mpesa_analyzer.interfaces.JsonProcesses
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import com.niccher.my_mpesa_analyzer.models.Mod_Summaries
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class Frag_History : Fragment() {

    private var _binding: FragHistoryBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var connState: TextView
    private lateinit var connWait: ProgressBar

    private lateinit var jsonProcesses: JsonProcesses
    private lateinit var kon: Konstants
    private lateinit var summariesAdapter: Adapter_Frag_History
    private lateinit var pref: Prefs

    private var gson: Gson = GsonBuilder().setLenient().create()
//    private var summariesList: ArrayList<Mod_Summaries> = ArrayList()
    private var summariesList: MutableList<Mod_Summaries> = mutableListOf()


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        kon = Konstants
        pref = Prefs()

        gson = GsonBuilder()
            .setLenient()
            .create()
        
        val fragHistory = inflater.inflate(R.layout.frag_history, container, false);

        recyclerView = fragHistory.findViewById(R.id.recy_history)
        connState = fragHistory.findViewById(R.id.conn_no_internet)
        connWait = fragHistory.findViewById(R.id.conn_wait_internet)
        connWait.visibility = View.GONE
        connState.visibility = View.GONE

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load data ONCE — uses cache when offline
        viewLifecycleOwner.lifecycleScope.launch {
            getSummaries()
        }

        return fragHistory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle("History")
    }

    override fun onResume() {
        super.onResume()
        setTitle("History")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setTitle(title: String) {
        // Get the activity and set the title
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = title
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun isOffline(msg: String) {
        connWait.visibility = View.GONE
        connState.visibility = View.VISIBLE
        connState.text = msg
        recyclerView.visibility = View.GONE
    }

    private suspend fun getSummaries() {
        try {
            connWait.visibility = View.VISIBLE

            val jsonProcesses = ServiceGenerators.createService(JsonProcesses::class.java, requireContext())
            val parameters = mapOf(
                "varUser" to pref.getPrefsAuth("auth", requireContext()),
                "varDev" to pref.getPrefsAuth("print", requireActivity())
            )

            val response = jsonProcesses.getSummary(parameters)
            summariesList = response.summarizer?.toMutableList() ?: mutableListOf()

            if (summariesList.isNotEmpty()) {
                summariesAdapter = Adapter_Frag_History(summariesList as ArrayList<Mod_Summaries>, requireActivity())
                recyclerView.adapter = summariesAdapter
                summariesAdapter.notifyDataSetChanged()

                connWait.visibility = View.GONE
                connState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                isOffline("No data available")
            }

        } catch (e: Exception) {
            connWait.visibility = View.GONE
            isOffline("Offline • Showing cached data")
            Log.e(kon.TAGGED, "History: ${e.message}", e)
        }
    }

}