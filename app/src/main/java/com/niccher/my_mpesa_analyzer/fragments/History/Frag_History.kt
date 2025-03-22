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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    private var summariesList: ArrayList<Mod_Summaries> = ArrayList()


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

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getConnectionState()

        return fragHistory
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getConnectionState() {
        if (isConnected()) {
            connWait.visibility = View.VISIBLE
            connState.visibility = View.GONE
            getSummaries()
        } else {
            connWait.visibility = View.GONE
            recyclerView.visibility = View.GONE
            isOffline("No internet connection at the moment")
        }
    }

    fun isConnected(): Boolean {
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo?.isConnected == true
    }

    private fun isOffline(msg: String) {
        connState.text = msg
    }

    private fun getSummaries() {
        connWait.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl(kon.LINK_PROCESS)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(ServiceGenerators.getUnsafeOkHttpClient())
            .build()

        jsonProcesses = retrofit.create(JsonProcesses::class.java)

        val parameters = mutableMapOf<String, String>()
        parameters["varUser"] = pref.getPrefsAuth("auth", requireContext())
        parameters["varDev"] = pref.getPrefsAuth("print", requireActivity())

        val call = jsonProcesses.getSummary(parameters)
        call.enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(call: Call<SummaryResponse>, response: Response<SummaryResponse>) {
                connWait.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val summaryResponse = response.body()!!
                    summariesList = ArrayList(summaryResponse.summarizer?.toList().orEmpty())
                    summariesAdapter = Adapter_Frag_History(summariesList, requireActivity())
                    recyclerView.adapter = summariesAdapter
                    summariesAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                connWait.visibility = View.GONE
                isOffline("Unknown error has occurred, please try again later")
                Toast.makeText(context, t.message ?: "Unknown error", Toast.LENGTH_LONG).show()
                Log.e(kon.TAGGED, t.message ?: "Unknown error")
            }
        })

    }

}