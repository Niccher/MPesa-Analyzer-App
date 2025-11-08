package com.niccher.my_mpesa_analyzer.fragments.History

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.helpers.Prefs
import com.niccher.my_mpesa_analyzer.interfaces.JsonProcesses
import com.niccher.my_mpesa_analyzer.konstants.Konstants
import com.niccher.my_mpesa_analyzer.models.Mod_Loot_Summary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Frag_Summary : Fragment() {

    private lateinit var kon: Konstants
    private lateinit var activity: AppCompatActivity

    private lateinit var tv_gen_all: TextView
    private lateinit var tv_gen_bal: TextView
    private lateinit var tv_gen_fuliza: TextView
    private lateinit var tv_gen_recv: TextView
    private lateinit var tv_gen_sent: TextView
    private lateinit var tv_gen_withdraw: TextView
    private lateinit var tv_gen_wrong_pin: TextView
    private lateinit var tv_gen_unknown: TextView
    private lateinit var tv_gen_time: TextView
    private lateinit var tv_gen_date: TextView

    private lateinit var tv_sent_all: TextView
    private lateinit var tv_sent_mpesa: TextView
    private lateinit var tv_sent_mshwari: TextView
    private lateinit var tv_sent_lnm: TextView
    private lateinit var tv_sent_mini: TextView
    private lateinit var tv_sent_cancel: TextView

    private lateinit var tv_rec_all: TextView
    private lateinit var tv_rec_bank: TextView
    private lateinit var tv_rec_mshwari: TextView
    private lateinit var tv_rec_ncba: TextView
    private lateinit var tv_rec_im: TextView
    private lateinit var tv_rec_kcb: TextView
    private lateinit var tv_rec_reversal: TextView

    private lateinit var tv_bal_all: TextView
    private lateinit var tv_bal_mpesa: TextView
    private lateinit var tv_bal_mshwari: TextView
    private lateinit var tv_bal_kcb: TextView

    private lateinit var tv_fuliza_all: TextView
    private lateinit var tv_fuliza_opt_in: TextView
    private lateinit var tv_fuliza_opt_out: TextView
    private lateinit var tv_fuliza_limit: TextView
    private lateinit var tv_fuliza_loan: TextView
    private lateinit var tv_fuliza_mini: TextView

    private lateinit var tv_error_all: TextView
    private lateinit var tv_error_pin: TextView
    private lateinit var tv_error_less: TextView
    private lateinit var tv_error_merchant: TextView
    private lateinit var tv_error_receiver: TextView
    private lateinit var tv_error_org: TextView
    private lateinit var tv_error_failed: TextView

    private lateinit var lin_lay_gen_all: LinearLayout
    private lateinit var lin_lay_gen_bal: LinearLayout
    private lateinit var lin_lay_gen_fuliza: LinearLayout
    private lateinit var lin_lay_gen_recv: LinearLayout
    private lateinit var lin_lay_gen_sent: LinearLayout
    private lateinit var lin_lay_gen_withdraw: LinearLayout
    private lateinit var lin_lay_gen_wrong_pin: LinearLayout
    private lateinit var lin_lay_gen_unknown: LinearLayout
    private lateinit var lin_lay_gen_time: LinearLayout
    private lateinit var lin_lay_gen_date: LinearLayout

    private lateinit var lin_lay_rec_all: LinearLayout
    private lateinit var lin_lay_rec_bank: LinearLayout
    private lateinit var lin_lay_rec_mshwari: LinearLayout
    private lateinit var lin_lay_rec_ncba: LinearLayout
    private lateinit var lin_lay_rec_im: LinearLayout
    private lateinit var lin_lay_rec_reversal: LinearLayout

    private lateinit var lin_lay_bal_all: LinearLayout
    private lateinit var lin_lay_bal_mpesa: LinearLayout
    private lateinit var lin_lay_bal_mshwari: LinearLayout
    private lateinit var lin_lay_bal_kcb: LinearLayout

    private lateinit var lin_lay_fuliza_all: LinearLayout
    private lateinit var lin_lay_fuliza_opt_in: LinearLayout
    private lateinit var lin_lay_fuliza_opt_out: LinearLayout
    private lateinit var lin_lay_fuliza_limit: LinearLayout
    private lateinit var lin_lay_fuliza_loan: LinearLayout
    private lateinit var lin_lay_fuliza_mini: LinearLayout

    private lateinit var lin_lay_error_all: LinearLayout
    private lateinit var lin_lay_error_pin: LinearLayout
    private lateinit var lin_lay_error_less: LinearLayout
    private lateinit var lin_lay_error_merchant: LinearLayout
    private lateinit var lin_lay_error_receiver: LinearLayout
    private lateinit var lin_lay_error_org: LinearLayout
    private lateinit var lin_lay_error_failed: LinearLayout

    private lateinit var lin_loot_delete: LinearLayout
    private lateinit var layout_interactions: LinearLayout

    private var lood_uuid: String = ""
    private lateinit var jsonProcesses: JsonProcesses
    private var gson: Gson? = null
    private lateinit var pref: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kon = Konstants
        pref = Prefs()

        gson = GsonBuilder()
            .setLenient()
            .create()

        activity = requireActivity() as AppCompatActivity
        val supportActionBar = activity.supportActionBar
        supportActionBar?.apply {
            title = "Summary Info"
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val summarizer = inflater.inflate(R.layout.frag_summary, container, false)

        tv_gen_all = summarizer.findViewById(R.id.cat_point_gen_all)
        tv_gen_bal = summarizer.findViewById(R.id.cat_point_gen_bal)
        tv_gen_fuliza = summarizer.findViewById(R.id.cat_point_gen_fuliza)
        tv_gen_recv = summarizer.findViewById(R.id.cat_point_gen_received)
        tv_gen_sent = summarizer.findViewById(R.id.cat_point_gen_sent)
        tv_gen_withdraw = summarizer.findViewById(R.id.cat_point_gen_withdraw)
        tv_gen_wrong_pin = summarizer.findViewById(R.id.cat_point_gen_wrong_pin)
        tv_gen_unknown = summarizer.findViewById(R.id.cat_point_gen_unknown)

        tv_sent_all = summarizer.findViewById(R.id.cat_point_sent_all)
        tv_sent_mpesa = summarizer.findViewById(R.id.cat_point_sent_mpesa)
        tv_sent_mshwari = summarizer.findViewById(R.id.cat_point_sent_mshwari)
        tv_sent_lnm = summarizer.findViewById(R.id.cat_point_sent_lnm)
        tv_sent_mini = summarizer.findViewById(R.id.cat_point_sent_mini)
        tv_sent_cancel = summarizer.findViewById(R.id.cat_point_sent_cancel)

        tv_rec_all = summarizer.findViewById(R.id.cat_point_rec_all)
        tv_rec_bank = summarizer.findViewById(R.id.cat_point_rec_bal)
        tv_rec_mshwari = summarizer.findViewById(R.id.cat_point_rec_mshwari)
        tv_rec_ncba = summarizer.findViewById(R.id.cat_point_rec_ncba)
        tv_rec_im = summarizer.findViewById(R.id.cat_point_rec_im)
        tv_rec_reversal = summarizer.findViewById(R.id.cat_point_rec_reversal)
        tv_rec_kcb = summarizer.findViewById(R.id.cat_point_rec_kcb)

        tv_bal_all = summarizer.findViewById(R.id.cat_point_bal_all)
        tv_bal_mpesa = summarizer.findViewById(R.id.cat_point_bal_mpesa)
        tv_bal_mshwari = summarizer.findViewById(R.id.cat_point_bal_mshwari)
        tv_bal_kcb = summarizer.findViewById(R.id.cat_point_bal_kcb)

        tv_fuliza_all = summarizer.findViewById(R.id.cat_point_fuliza_all)
        tv_fuliza_opt_in = summarizer.findViewById(R.id.cat_point_fuliza_opt_in)
        tv_fuliza_opt_out = summarizer.findViewById(R.id.cat_point_fuliza_opt_out)
        tv_fuliza_limit = summarizer.findViewById(R.id.cat_point_fuliza_limit)
        tv_fuliza_loan = summarizer.findViewById(R.id.cat_point_fuliza_loan)
        tv_fuliza_mini = summarizer.findViewById(R.id.cat_point_fuliza_mini)

        tv_error_all = summarizer.findViewById(R.id.cat_point_error_all)
        tv_error_pin = summarizer.findViewById(R.id.cat_point_error_pin)
        tv_error_less = summarizer.findViewById(R.id.cat_point_error_less)
        tv_error_merchant = summarizer.findViewById(R.id.cat_point_error_merchant)
        tv_error_receiver = summarizer.findViewById(R.id.cat_point_error_receiver)
        tv_error_org = summarizer.findViewById(R.id.cat_point_error_org)
        tv_error_failed = summarizer.findViewById(R.id.cat_point_error_failed)

        tv_gen_time = summarizer.findViewById(R.id.cat_point_loot_time)
        tv_gen_date = summarizer.findViewById(R.id.cat_point_loot_date)

        lin_lay_gen_all = summarizer.findViewById(R.id.summary_gen_All)
        lin_lay_gen_bal = summarizer.findViewById(R.id.summary_Gen_Balance)
        lin_lay_gen_fuliza = summarizer.findViewById(R.id.summary_Gen_Fuliza)
        lin_lay_gen_recv = summarizer.findViewById(R.id.summary_Gen_Received)
        lin_lay_gen_sent = summarizer.findViewById(R.id.summary_Gen_Sent)
        lin_lay_gen_withdraw = summarizer.findViewById(R.id.summary_Gen_Withdraw)
        lin_lay_gen_wrong_pin = summarizer.findViewById(R.id.summary_Gen_Wrong_Pin)
        lin_lay_gen_unknown = summarizer.findViewById(R.id.summary_Gen_Unknown)

        lin_lay_rec_all = summarizer.findViewById(R.id.summary_Received_All)
        lin_lay_rec_bank = summarizer.findViewById(R.id.summary_Rec_Mpesa)
        lin_lay_rec_mshwari = summarizer.findViewById(R.id.summary_Rec_Mshwari)
        lin_lay_rec_ncba = summarizer.findViewById(R.id.summary_Rec_NCBA)
        lin_lay_rec_im = summarizer.findViewById(R.id.summary_Rec_IM)
        lin_lay_rec_reversal = summarizer.findViewById(R.id.summary_Rec_Reversal)

        lin_lay_bal_all = summarizer.findViewById(R.id.summary_ball_All)
        lin_lay_bal_mpesa = summarizer.findViewById(R.id.summary_Bal_mpesa)
        lin_lay_bal_mshwari = summarizer.findViewById(R.id.summary_Bal_mshwari)
        lin_lay_bal_kcb = summarizer.findViewById(R.id.summary_Bal_KCB)

        lin_lay_fuliza_all = summarizer.findViewById(R.id.summary_Fuliza_All)
        lin_lay_fuliza_opt_in = summarizer.findViewById(R.id.summary_Fuliza_Opt_In)
        lin_lay_fuliza_opt_out = summarizer.findViewById(R.id.summary_Fuliza_Opt_Out)
        lin_lay_fuliza_limit = summarizer.findViewById(R.id.summary_Fuliza_Limit)
        lin_lay_fuliza_loan = summarizer.findViewById(R.id.summary_Fuliza_Loan)
        lin_lay_fuliza_mini = summarizer.findViewById(R.id.summary_Fuliza_Mini)

        lin_lay_error_all = summarizer.findViewById(R.id.summary_Error_All)
        lin_lay_error_pin = summarizer.findViewById(R.id.summary_Error_Pin)
        lin_lay_error_less = summarizer.findViewById(R.id.summary_Error_Less)
        lin_lay_error_merchant = summarizer.findViewById(R.id.summary_Error_Merchant)
        lin_lay_error_receiver = summarizer.findViewById(R.id.summary_Error_Receiver)
        lin_lay_error_org = summarizer.findViewById(R.id.summary_Error_Org)
        lin_lay_error_failed = summarizer.findViewById(R.id.summary_Error_Failed)

        lin_loot_delete = summarizer.findViewById(R.id.summary_Loot_Delete)
        layout_interactions = summarizer.findViewById(R.id.summary_interactions_layout)

        lood_uuid = ""

        getConnectionState()

        return summarizer
    }

    override fun onResume() {
        super.onResume()
        getConnectionState()
    }

    private fun getConnectionState() {
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo

        if (netInfo != null && netInfo.isConnected) {
            getReferences()
        } else {
            layout_interactions.visibility = View.GONE
            Toast.makeText(activity, "No internet connection at the moment", Toast.LENGTH_LONG).show()
        }
    }

    private fun getReferences() {
        val sent_data = arguments
        if (sent_data != null) {
            val st_name = sent_data.getString("summary_loot_name")
            if (!st_name.isNullOrEmpty()) {
                getSummaries(st_name)
            }
        }
    }

    private fun getClickedAction(count_Loot_Uuid: String) {
        lin_lay_gen_all.setOnClickListener { getSmsListingFor("lin_lay_gen_all") }
        lin_lay_gen_bal.setOnClickListener { getSmsListingFor("lin_lay_gen_bal") }
        lin_lay_gen_fuliza.setOnClickListener { getSmsListingFor("lin_lay_gen_fuliza") }
        lin_lay_gen_recv.setOnClickListener { getSmsListingFor("lin_lay_gen_recv") }
        lin_lay_gen_sent.setOnClickListener { getSmsListingFor("lin_lay_gen_sent") }
        lin_lay_gen_withdraw.setOnClickListener { getSmsListingFor("lin_lay_gen_withdraw") }
        lin_lay_gen_wrong_pin.setOnClickListener { getSmsListingFor("lin_lay_gen_wrong_pin") }
        lin_lay_gen_unknown.setOnClickListener { getSmsListingFor("lin_lay_gen_unknown") }

        lin_lay_rec_all.setOnClickListener { getSmsListingFor("lin_lay_rec_all") }
        lin_lay_rec_bank.setOnClickListener { getSmsListingFor("Received from Mpesa") }
        lin_lay_rec_mshwari.setOnClickListener { getSmsListingFor("Received from Mshwari") }
        lin_lay_rec_ncba.setOnClickListener { getSmsListingFor("Received from NCBA") }
        lin_lay_rec_im.setOnClickListener { getSmsListingFor("Received from IM") }
        lin_lay_rec_reversal.setOnClickListener { getSmsListingFor("Received from Reversal") }

        lin_lay_bal_all.setOnClickListener { getSmsListingFor("lin_lay_bal_all") }
        lin_lay_bal_mpesa.setOnClickListener { getSmsListingFor("Get MPESA Balance") }
        lin_lay_bal_mshwari.setOnClickListener { getSmsListingFor("Get MShwari Balance") }
        lin_lay_bal_kcb.setOnClickListener { getSmsListingFor("Get KCB Balance") }

        lin_lay_fuliza_all.setOnClickListener { getSmsListingFor("lin_lay_fuliza_all") }
        lin_lay_fuliza_opt_in.setOnClickListener { getSmsListingFor("Fuliza Opt In") }
        lin_lay_fuliza_opt_out.setOnClickListener { getSmsListingFor("Fuliza Leave") }
        lin_lay_fuliza_limit.setOnClickListener { getSmsListingFor("Fuliza Limit") }
        lin_lay_fuliza_loan.setOnClickListener { getSmsListingFor("Fuliza Loan Taken") }
        lin_lay_fuliza_mini.setOnClickListener { getSmsListingFor("Fuliza Mini Statement") }

        lin_lay_error_all.setOnClickListener { getSmsListingFor("lin_lay_error_all") }
        lin_lay_error_pin.setOnClickListener { getSmsListingFor("Wrong Pin") }
        lin_lay_error_less.setOnClickListener { getSmsListingFor("Insufficient funds") }
        lin_lay_error_merchant.setOnClickListener { getSmsListingFor("Wrong Merchant") }
        lin_lay_error_receiver.setOnClickListener { getSmsListingFor("Receiver not in Service") }
        lin_lay_error_org.setOnClickListener { getSmsListingFor("Org not in Service") }
        lin_lay_error_failed.setOnClickListener { getSmsListingFor("Transaction Cancelled") }
        lin_loot_delete.setOnClickListener { getSmsListingFor("Delete Loot") }
    }

    private fun getSmsListingFor(category: String) {
        if (category.isNotEmpty()) {
            Toast.makeText(activity, "Category as $category", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Null Category", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSummaries(loot_name: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(kon.LINK_PROCESS)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(ServiceGenerators.getUnsafeOkHttpClient())
            .build()

        jsonProcesses = retrofit.create(JsonProcesses::class.java)

        val parameters = HashMap<String, String>()
        parameters["varUser"] = pref.getPrefsAuth("auth", requireContext())
        parameters["varDev"] = pref.getPrefsAuth("print", requireActivity())
        parameters["varLootUuid"] = loot_name

        val call = jsonProcesses.getSummaryCalc(parameters)
        call.enqueue(object : Callback<Mod_Loot_Summary> {
            override fun onResponse(call: Call<Mod_Loot_Summary>, response: Response<Mod_Loot_Summary>) {
                if (response.isSuccessful && response.body() != null) {
                    val mod_loot_summary = response.body()!!

                    var all_rec = 0
                    var all_sent = 0
                    var all_bal = 0
                    var all_fuliza = 0
                    var all_error = 0

                    tv_gen_all.text = mod_loot_summary.loot_summarizer.count_All
                    tv_gen_bal.text = mod_loot_summary.loot_summarizer.count_Get_Bal_MPESA
                    tv_gen_fuliza.text = mod_loot_summary.loot_summarizer.count_Fuliza_Mini_Statement
                    tv_gen_recv.text = mod_loot_summary.loot_summarizer.count_Get_from_MPESA
                    tv_gen_sent.text = mod_loot_summary.loot_summarizer.count_Sent_to_MPESA
                    tv_gen_withdraw.text = mod_loot_summary.loot_summarizer.count_Withdraw
                    tv_gen_wrong_pin.text = mod_loot_summary.loot_summarizer.count_Error_Pin
                    tv_gen_unknown.text = mod_loot_summary.loot_summarizer.count_Unknown

                    all_sent = mod_loot_summary.loot_summarizer.count_Sent_to_MPESA.toInt() + mod_loot_summary.loot_summarizer.count_Sent_to_Mshwari.toInt() +
                            mod_loot_summary.loot_summarizer.count_Sent_to_LNM.toInt() + mod_loot_summary.loot_summarizer.count_Sent_Mini.toInt() +
                            mod_loot_summary.loot_summarizer.count_Sent_Cancel.toInt()
                    tv_sent_all.text = all_sent.toString()
                    tv_sent_mpesa.text = mod_loot_summary.loot_summarizer.count_Sent_to_MPESA
                    tv_sent_mshwari.text = mod_loot_summary.loot_summarizer.count_Sent_to_Mshwari
                    tv_sent_lnm.text = mod_loot_summary.loot_summarizer.count_Sent_to_LNM
                    tv_sent_mini.text = mod_loot_summary.loot_summarizer.count_Sent_Mini
                    tv_sent_cancel.text = mod_loot_summary.loot_summarizer.count_Sent_Cancel

                    all_rec = mod_loot_summary.loot_summarizer.count_Get_from_MPESA.toInt() + mod_loot_summary.loot_summarizer.count_Get_from_KCB.toInt() +
                            mod_loot_summary.loot_summarizer.count_Get_from_NCBA.toInt() + mod_loot_summary.loot_summarizer.count_Get_from_Mshwari.toInt() +
                            mod_loot_summary.loot_summarizer.count_Get_from_IM.toInt() + mod_loot_summary.loot_summarizer.count_Get_from_Reversal.toInt()
                    tv_rec_all.text = all_rec.toString()
                    tv_rec_bank.text = mod_loot_summary.loot_summarizer.count_Get_from_MPESA // Mpesa
                    tv_rec_mshwari.text = mod_loot_summary.loot_summarizer.count_Get_from_Mshwari
                    tv_rec_ncba.text = mod_loot_summary.loot_summarizer.count_Get_from_NCBA

                    tv_rec_im.text = mod_loot_summary.loot_summarizer.count_Get_from_IM
                    tv_rec_kcb.text = mod_loot_summary.loot_summarizer.count_Get_from_KCB
                    tv_rec_reversal.text = mod_loot_summary.loot_summarizer.count_Get_from_Reversal

                    all_bal = mod_loot_summary.loot_summarizer.count_Get_Bal_KCB.toInt() + mod_loot_summary.loot_summarizer.count_Get_Bal_Mshwari.toInt() +
                            mod_loot_summary.loot_summarizer.count_Get_Bal_MPESA.toInt()
                    tv_bal_all.text = all_bal.toString()
                    tv_bal_mpesa.text = mod_loot_summary.loot_summarizer.count_Get_Bal_MPESA
                    tv_bal_mshwari.text = mod_loot_summary.loot_summarizer.count_Get_Bal_Mshwari
                    tv_bal_kcb.text = mod_loot_summary.loot_summarizer.count_Get_Bal_KCB

                    all_fuliza = mod_loot_summary.loot_summarizer.count_Fuliza_Opt_In.toInt() +
                            mod_loot_summary.loot_summarizer.count_Fuliza_Limit.toInt() + mod_loot_summary.loot_summarizer.count_Fuliza_Opt_Out.toInt() +
                            mod_loot_summary.loot_summarizer.count_Fuliza_Loan_Taken.toInt() + mod_loot_summary.loot_summarizer.count_Fuliza_Mini_Statement.toInt()
                    tv_fuliza_all.text = all_fuliza.toString()
                    tv_fuliza_opt_in.text = mod_loot_summary.loot_summarizer.count_Fuliza_Opt_In
                    tv_fuliza_opt_out.text = mod_loot_summary.loot_summarizer.count_Fuliza_Opt_Out
                    tv_fuliza_limit.text = mod_loot_summary.loot_summarizer.count_Fuliza_Limit
                    tv_fuliza_loan.text = mod_loot_summary.loot_summarizer.count_Fuliza_Loan_Taken
                    tv_fuliza_mini.text = mod_loot_summary.loot_summarizer.count_Fuliza_Mini_Statement

                    all_error = mod_loot_summary.loot_summarizer.count_Error_Pin.toInt() + mod_loot_summary.loot_summarizer.count_Error_Less.toInt() +
                            mod_loot_summary.loot_summarizer.count_Error_Receiver.toInt() +
                            mod_loot_summary.loot_summarizer.count_Error_Receiver_Org.toInt() + mod_loot_summary.loot_summarizer.count_Error_Failed.toInt()
                    tv_error_all.text = all_error.toString()
                    tv_error_pin.text = mod_loot_summary.loot_summarizer.count_Error_Pin
                    tv_error_less.text = mod_loot_summary.loot_summarizer.count_Error_Less
                    tv_error_merchant.text = "0"
                    tv_error_receiver.text = mod_loot_summary.loot_summarizer.count_Error_Receiver
                    tv_error_org.text = mod_loot_summary.loot_summarizer.count_Error_Receiver_Org
                    tv_error_failed.text = mod_loot_summary.loot_summarizer.count_Error_Failed

                    val lootCreated = mod_loot_summary.loot_summarizer.loot_Created.split(" ")
                    tv_gen_time.text = lootCreated[1]
                    tv_gen_date.text = lootCreated[0]

                    getClickedAction(mod_loot_summary.loot_summarizer.loot_Uuid)
                }
            }

            override fun onFailure(call: Call<Mod_Loot_Summary>, t: Throwable) {
                Log.e(kon.TAGGED, "**********************: onFailure Unknown error occurred, please try again")
                Log.e(kon.TAGGED, t.message ?: "Unknown error")
            }
        })
    }

    private fun setLootToDelete(loot_uuid: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(kon.LINK_PROCESS)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(ServiceGenerators.getUnsafeOkHttpClient())
            .build()

        jsonProcesses = retrofit.create(JsonProcesses::class.java)

        val parameters = HashMap<String, String>()
        parameters["varUser"] = pref.getPrefsAuth("auth", requireContext())
        parameters["varDev"] = pref.getPrefsAuth("print", requireActivity())
        parameters["varLootUuid"] = loot_uuid

//        val call = jsonProcesses.getLootDelete(parameters)
//        call.enqueue(object : Callback<Mod_Loot_Delete> {
//            override fun onResponse(call: Call<Mod_Loot_Delete>, response: Response<Mod_Loot_Delete>) {
//                if (response.isSuccessful && response.body() != null) {
//                    val mod_summary = response.body()!!
//                    when (mod_summary.summary_Status) {
//                        "1" -> {
//                            // Handle status 1
//                        }
//                        "2" -> {
//                            // Handle status 2
//                        }
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<Mod_Loot_Delete>, t: Throwable) {
//                Log.e(kon.TAGGED, "**********************: onFailure Unknown error occurred, please try again")
//                Log.e(kon.TAGGED, t.message ?: "Unknown error")
//            }
//        })
    }

    private fun backtoHistory() {
        val frag_history = Frag_History()
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_bottom, frag_history)
        fragmentTransaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> backtoHistory()
        }
        return super.onOptionsItemSelected(item)
    }
}