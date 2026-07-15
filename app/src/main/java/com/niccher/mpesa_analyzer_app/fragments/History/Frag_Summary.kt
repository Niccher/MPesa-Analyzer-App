package com.niccher.mpesa_analyzer_app.fragments.History

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.interfaces.JsonProcesses
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_Loot_Summary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Frag_Summary : Fragment() {

    private lateinit var kon: Konstants
    private lateinit var activity: AppCompatActivity
    private lateinit var pref: Prefs

    // Header views
    private lateinit var tvGenDate: TextView
    private lateinit var tvGenTime: TextView
    private lateinit var tvTotalInteractions: TextView

    // Quick stats
    private lateinit var tvQuickReceived: TextView
    private lateinit var tvQuickSent: TextView
    private lateinit var tvQuickUnknown: TextView
    private lateinit var tvQuickFuliza: TextView

    // Section content containers
    private lateinit var llGeneralContent: LinearLayout
    private lateinit var llSentContent: LinearLayout
    private lateinit var llReceivedContent: LinearLayout
    private lateinit var llBalanceContent: LinearLayout
    private lateinit var llFulizaContent: LinearLayout
    private lateinit var llErrorsContent: LinearLayout

    // Expand icons
    private lateinit var ivExpandGeneral: ImageView
    private lateinit var ivExpandSent: ImageView
    private lateinit var ivExpandReceived: ImageView
    private lateinit var ivExpandBalance: ImageView
    private lateinit var ivExpandFuliza: ImageView
    private lateinit var ivExpandErrors: ImageView

    // Metric value TextViews - General
    private lateinit var tvGenAll: TextView
    private lateinit var tvGenBalance: TextView
    private lateinit var tvGenFuliza: TextView
    private lateinit var tvGenReceived: TextView
    private lateinit var tvGenSent: TextView
    private lateinit var tvGenWithdraw: TextView
    private lateinit var tvGenWrongPin: TextView
    private lateinit var tvGenUnknown: TextView

    // Metric value TextViews - Sent
    private lateinit var tvSentAll: TextView
    private lateinit var tvSentMpesa: TextView
    private lateinit var tvSentMshwari: TextView
    private lateinit var tvSentLnm: TextView
    private lateinit var tvSentMini: TextView
    private lateinit var tvSentCancel: TextView

    // Metric value TextViews - Received
    private lateinit var tvRecAll: TextView
    private lateinit var tvRecMpesa: TextView
    private lateinit var tvRecMshwari: TextView
    private lateinit var tvRecNcba: TextView
    private lateinit var tvRecIm: TextView
    private lateinit var tvRecReversal: TextView
    private lateinit var tvRecKcb: TextView

    // Metric value TextViews - Balance
    private lateinit var tvBalAll: TextView
    private lateinit var tvBalMpesa: TextView
    private lateinit var tvBalMshwari: TextView
    private lateinit var tvBalKcb: TextView

    // Metric value TextViews - Fuliza
    private lateinit var tvFulizaAll: TextView
    private lateinit var tvFulizaOptIn: TextView
    private lateinit var tvFulizaOptOut: TextView
    private lateinit var tvFulizaLimit: TextView
    private lateinit var tvFulizaLoan: TextView
    private lateinit var tvFulizaMini: TextView

    // Metric value TextViews - Errors
    private lateinit var tvErrorAll: TextView
    private lateinit var tvErrorPin: TextView
    private lateinit var tvErrorLess: TextView
    private lateinit var tvErrorReceiver: TextView
    private lateinit var tvErrorOrg: TextView
    private lateinit var tvErrorFailed: TextView

    // Other views
    private lateinit var btnDeleteSummary: com.google.android.material.button.MaterialButton
    private lateinit var layoutInteractions: LinearLayout
    private lateinit var progressBar: LinearLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var emptyState: LinearLayout

    private var lootUuid: String = ""
    private lateinit var jsonProcesses: JsonProcesses
    private var gson: Gson? = null

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
            setDisplayShowHomeEnabled(true)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_summary, container, false)

        // Header views
        tvGenDate = view.findViewById(R.id.cat_point_loot_date)
        tvGenTime = view.findViewById(R.id.cat_point_loot_time)
        tvTotalInteractions = view.findViewById(R.id.tv_total_interactions)

        // Quick stats
        tvQuickReceived = view.findViewById(R.id.tv_quick_received)
        tvQuickSent = view.findViewById(R.id.tv_quick_sent)
        tvQuickUnknown = view.findViewById(R.id.tv_quick_unknown)
        tvQuickFuliza = view.findViewById(R.id.tv_quick_fuliza)

        // Content containers
        llGeneralContent = view.findViewById(R.id.ll_general_content)
        llSentContent = view.findViewById(R.id.ll_sent_content)
        llReceivedContent = view.findViewById(R.id.ll_received_content)
        llBalanceContent = view.findViewById(R.id.ll_balance_content)
        llFulizaContent = view.findViewById(R.id.ll_fuliza_content)
        llErrorsContent = view.findViewById(R.id.ll_errors_content)

        // Expand icons
        ivExpandGeneral = view.findViewById(R.id.iv_expand_general)
        ivExpandSent = view.findViewById(R.id.iv_expand_sent)
        ivExpandReceived = view.findViewById(R.id.iv_expand_received)
        ivExpandBalance = view.findViewById(R.id.iv_expand_balance)
        ivExpandFuliza = view.findViewById(R.id.iv_expand_fuliza)
        ivExpandErrors = view.findViewById(R.id.iv_expand_errors)

        // General metrics
        tvGenAll = view.findViewById<View>(R.id.item_gen_all).findViewById<TextView>(R.id.tv_metric_value)
        tvGenBalance = view.findViewById<View>(R.id.item_gen_balance).findViewById<TextView>(R.id.tv_metric_value)
        tvGenFuliza = view.findViewById<View>(R.id.item_gen_fuliza).findViewById<TextView>(R.id.tv_metric_value)
        tvGenReceived = view.findViewById<View>(R.id.item_gen_received).findViewById<TextView>(R.id.tv_metric_value)
        tvGenSent = view.findViewById<View>(R.id.item_gen_sent).findViewById<TextView>(R.id.tv_metric_value)
        tvGenWithdraw = view.findViewById<View>(R.id.item_gen_withdraw).findViewById<TextView>(R.id.tv_metric_value)
        tvGenWrongPin = view.findViewById<View>(R.id.item_gen_wrong_pin).findViewById<TextView>(R.id.tv_metric_value)
        tvGenUnknown = view.findViewById<View>(R.id.item_gen_unknown).findViewById<TextView>(R.id.tv_metric_value)

        // Sent metrics
        tvSentAll = view.findViewById<View>(R.id.item_sent_all).findViewById<TextView>(R.id.tv_metric_value)
        tvSentMpesa = view.findViewById<View>(R.id.item_sent_mpesa).findViewById<TextView>(R.id.tv_metric_value)
        tvSentMshwari = view.findViewById<View>(R.id.item_sent_mshwari).findViewById<TextView>(R.id.tv_metric_value)
        tvSentLnm = view.findViewById<View>(R.id.item_sent_lnm).findViewById<TextView>(R.id.tv_metric_value)
        tvSentMini = view.findViewById<View>(R.id.item_sent_mini).findViewById<TextView>(R.id.tv_metric_value)
        tvSentCancel = view.findViewById<View>(R.id.item_sent_cancel).findViewById<TextView>(R.id.tv_metric_value)

        // Received metrics
        tvRecAll = view.findViewById<View>(R.id.item_rec_all).findViewById<TextView>(R.id.tv_metric_value)
        tvRecMpesa = view.findViewById<View>(R.id.item_rec_mpesa).findViewById<TextView>(R.id.tv_metric_value)
        tvRecMshwari = view.findViewById<View>(R.id.item_rec_mshwari).findViewById<TextView>(R.id.tv_metric_value)
        tvRecNcba = view.findViewById<View>(R.id.item_rec_ncba).findViewById<TextView>(R.id.tv_metric_value)
        tvRecIm = view.findViewById<View>(R.id.item_rec_im).findViewById<TextView>(R.id.tv_metric_value)
        tvRecReversal = view.findViewById<View>(R.id.item_rec_reversal).findViewById<TextView>(R.id.tv_metric_value)
        tvRecKcb = view.findViewById<View>(R.id.item_rec_kcb).findViewById<TextView>(R.id.tv_metric_value)

        // Balance metrics
        tvBalAll = view.findViewById<View>(R.id.item_bal_all).findViewById<TextView>(R.id.tv_metric_value)
        tvBalMpesa = view.findViewById<View>(R.id.item_bal_mpesa).findViewById<TextView>(R.id.tv_metric_value)
        tvBalMshwari = view.findViewById<View>(R.id.item_bal_mshwari).findViewById<TextView>(R.id.tv_metric_value)
        tvBalKcb = view.findViewById<View>(R.id.item_bal_kcb).findViewById<TextView>(R.id.tv_metric_value)

        // Fuliza metrics
        tvFulizaAll = view.findViewById<View>(R.id.item_fuliza_all).findViewById<TextView>(R.id.tv_metric_value)
        tvFulizaOptIn = view.findViewById<View>(R.id.item_fuliza_opt_in).findViewById<TextView>(R.id.tv_metric_value)
        tvFulizaOptOut = view.findViewById<View>(R.id.item_fuliza_opt_out).findViewById<TextView>(R.id.tv_metric_value)
        tvFulizaLimit = view.findViewById<View>(R.id.item_fuliza_limit).findViewById<TextView>(R.id.tv_metric_value)
        tvFulizaLoan = view.findViewById<View>(R.id.item_fuliza_loan).findViewById<TextView>(R.id.tv_metric_value)
        tvFulizaMini = view.findViewById<View>(R.id.item_fuliza_mini).findViewById<TextView>(R.id.tv_metric_value)

        // Error metrics
        tvErrorAll = view.findViewById<View>(R.id.item_error_all).findViewById<TextView>(R.id.tv_metric_value)
        tvErrorPin = view.findViewById<View>(R.id.item_error_pin).findViewById<TextView>(R.id.tv_metric_value)
        tvErrorLess = view.findViewById<View>(R.id.item_error_less).findViewById<TextView>(R.id.tv_metric_value)
        tvErrorReceiver = view.findViewById<View>(R.id.item_error_receiver).findViewById<TextView>(R.id.tv_metric_value)
        tvErrorOrg = view.findViewById<View>(R.id.item_error_org).findViewById<TextView>(R.id.tv_metric_value)
        tvErrorFailed = view.findViewById<View>(R.id.item_error_failed).findViewById<TextView>(R.id.tv_metric_value)

        // Other views
        btnDeleteSummary = view.findViewById(R.id.btn_delete_summary)
        layoutInteractions = view.findViewById(R.id.summary_interactions_layout)
        progressBar = view.findViewById(R.id.summary_progress_bar)
        scrollView = view.findViewById(R.id.summary_scroll_view)
        emptyState = view.findViewById(R.id.summary_error_state)

        // Setup expand/collapse listeners
        setupExpandListeners()

        // Delete button
        btnDeleteSummary.setOnClickListener {
            if (lootUuid.isNotEmpty()) {
                Toast.makeText(requireContext(), "Delete not implemented", Toast.LENGTH_SHORT).show()
            }
        }

        // Retry button
        view.findViewById<TextView>(R.id.tv_retry).setOnClickListener {
            getConnectionState()
        }

        getConnectionState()

        return view
    }

    private fun setupExpandListeners() {
        val expandPairs = listOf(
            ivExpandGeneral to llGeneralContent,
            ivExpandSent to llSentContent,
            ivExpandReceived to llReceivedContent,
            ivExpandBalance to llBalanceContent,
            ivExpandFuliza to llFulizaContent,
            ivExpandErrors to llErrorsContent
        )

        expandPairs.forEach { (icon, content) ->
            icon.setOnClickListener {
                val isVisible = content.visibility == View.VISIBLE
                content.visibility = if (isVisible) View.GONE else View.VISIBLE
                icon.animate().rotation(if (isVisible) 0f else 180f).duration = 200
                icon.startAnimation(RotateAnimation(
                    if (isVisible) 180f else 0f,
                    if (isVisible) 0f else 180f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
                ).apply { duration = 200; fillAfter = true })
            }
        }
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
            showOfflineState()
        }
    }

    private fun showOfflineState() {
        progressBar.visibility = View.GONE
        scrollView.visibility = View.GONE
        layoutInteractions.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }

    private fun getReferences() {
        val sentData = arguments
        if (sentData != null) {
            val stName = sentData.getString("summary_loot_name")
            if (!stName.isNullOrEmpty()) {
                getSummaries(stName)
            }
        }
    }

    private fun getSummaries(lootName: String) {
        val jsonProcesses = ServiceGenerators.createService(JsonProcesses::class.java, requireContext())

        val parameters = HashMap<String, String>()
        parameters["varUser"] = pref.getPrefsAuth("auth", requireContext())
        parameters["varDev"] = pref.getPrefsAuth("print", requireActivity())
        parameters["varLootUuid"] = lootName

        val call = jsonProcesses.getSummaryCalc(parameters)

        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        layoutInteractions.visibility = View.GONE
        emptyState.visibility = View.GONE

        call.enqueue(object : Callback<Mod_Loot_Summary> {
            override fun onResponse(call: Call<Mod_Loot_Summary>, response: Response<Mod_Loot_Summary>) {
                if (response.isSuccessful && response.body() != null) {
                    progressBar.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    layoutInteractions.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE

                    val modLootSummary = response.body()!!

                    val s = modLootSummary.loot_summarizer

                    // Header date/time
                    try {
                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        val dateOutputFormat = java.text.SimpleDateFormat("EEEE, MMM dd", java.util.Locale.getDefault())
                        val timeOutputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        val parsedDate = inputFormat.parse(s.loot_Created)
                        if (parsedDate != null) {
                            tvGenDate.text = java.text.SimpleDateFormat("EEEE, MMM dd", java.util.Locale.getDefault()).format(parsedDate)
                            tvGenTime.text = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(parsedDate)
                        } else {
                            val lootCreated = s.loot_Created.split(" ")
                            tvGenDate.text = lootCreated[0]
                            tvGenTime.text = lootCreated.getOrNull(1) ?: ""
                        }
                    } catch (e: Exception) {
                        val lootCreated = s.loot_Created.split(" ")
                        tvGenDate.text = lootCreated[0]
                        tvGenTime.text = lootCreated.getOrNull(1) ?: ""
                    }

                    // Quick stats
                    val allSent = (s.count_Sent_to_MPESA.toIntOrNull() ?: 0) +
                            (s.count_Sent_to_Mshwari.toIntOrNull() ?: 0) +
                            (s.count_Sent_to_LNM.toIntOrNull() ?: 0) +
                            (s.count_Sent_Mini.toIntOrNull() ?: 0) +
                            (s.count_Sent_Cancel.toIntOrNull() ?: 0)
                    val allRec = (s.count_Get_from_MPESA.toIntOrNull() ?: 0) +
                            (s.count_Get_from_KCB.toIntOrNull() ?: 0) +
                            (s.count_Get_from_NCBA.toIntOrNull() ?: 0) +
                            (s.count_Get_from_Mshwari.toIntOrNull() ?: 0) +
                            (s.count_Get_from_IM.toIntOrNull() ?: 0) +
                            (s.count_Get_from_Reversal.toIntOrNull() ?: 0)
                    val allFuliza = (s.count_Fuliza_Opt_In.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Limit.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Opt_Out.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Loan_Taken.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Mini_Statement.toIntOrNull() ?: 0)

                    tvQuickReceived.text = allRec.toString()
                    tvQuickSent.text = allSent.toString()
                    tvQuickUnknown.text = s.count_Unknown
                    tvQuickFuliza.text = allFuliza.toString()
                    tvTotalInteractions.text = "${s.count_All} Interactions"

                    // General metrics
                    tvGenAll.text = s.count_All
                    tvGenBalance.text = s.count_Get_Bal_MPESA
                    tvGenFuliza.text = s.count_Fuliza_Mini_Statement
                    tvGenReceived.text = s.count_Get_from_MPESA
                    tvGenSent.text = s.count_Sent_to_MPESA
                    tvGenWithdraw.text = s.count_Withdraw
                    tvGenWrongPin.text = s.count_Error_Pin
                    tvGenUnknown.text = s.count_Unknown

                    // Sent breakdown
                    tvSentAll.text = allSent.toString()
                    tvSentMpesa.text = s.count_Sent_to_MPESA
                    tvSentMshwari.text = s.count_Sent_to_Mshwari
                    tvSentLnm.text = s.count_Sent_to_LNM
                    tvSentMini.text = s.count_Sent_Mini
                    tvSentCancel.text = s.count_Sent_Cancel

                    // Received breakdown
                    tvRecAll.text = allRec.toString()
                    tvRecMpesa.text = s.count_Get_from_MPESA
                    tvRecMshwari.text = s.count_Get_from_Mshwari
                    tvRecNcba.text = s.count_Get_from_NCBA
                    tvRecIm.text = s.count_Get_from_IM
                    tvRecReversal.text = s.count_Get_from_Reversal
                    tvRecKcb.text = s.count_Get_from_KCB

                    // Balance breakdown
                    val allBal = (s.count_Get_Bal_KCB.toIntOrNull() ?: 0) +
                            (s.count_Get_Bal_Mshwari.toIntOrNull() ?: 0) +
                            (s.count_Get_Bal_MPESA.toIntOrNull() ?: 0)
                    tvBalAll.text = allBal.toString()
                    tvBalMpesa.text = s.count_Get_Bal_MPESA
                    tvBalMshwari.text = s.count_Get_Bal_Mshwari
                    tvBalKcb.text = s.count_Get_Bal_KCB

                    // Fuliza breakdown
                    val allFulizaCalc = (s.count_Fuliza_Opt_In.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Limit.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Opt_Out.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Loan_Taken.toIntOrNull() ?: 0) +
                            (s.count_Fuliza_Mini_Statement.toIntOrNull() ?: 0)
                    tvFulizaAll.text = allFulizaCalc.toString()
                    tvFulizaOptIn.text = s.count_Fuliza_Opt_In
                    tvFulizaOptOut.text = s.count_Fuliza_Opt_Out
                    tvFulizaLimit.text = s.count_Fuliza_Limit
                    tvFulizaLoan.text = s.count_Fuliza_Loan_Taken
                    tvFulizaMini.text = s.count_Fuliza_Mini_Statement

                    // Errors breakdown
                    val allError = (s.count_Error_Pin.toIntOrNull() ?: 0) +
                            (s.count_Error_Less.toIntOrNull() ?: 0) +
                            (s.count_Error_Receiver.toIntOrNull() ?: 0) +
                            (s.count_Error_Receiver_Org.toIntOrNull() ?: 0) +
                            (s.count_Error_Failed.toIntOrNull() ?: 0)
                    tvErrorAll.text = allError.toString()
                    tvErrorPin.text = s.count_Error_Pin
                    tvErrorLess.text = s.count_Error_Less
                    tvErrorReceiver.text = s.count_Error_Receiver
                    tvErrorOrg.text = s.count_Error_Receiver_Org
                    tvErrorFailed.text = s.count_Error_Failed

                    lootUuid = s.loot_Uuid
                    setupClickListeners(s.loot_Uuid)
                }
            }

            override fun onFailure(call: Call<Mod_Loot_Summary>, t: Throwable) {
                progressBar.visibility = View.GONE
                showOfflineState()
                Log.e(kon.TAGGED, "Summary fetch failed: ${t.message}")
            }
        })
    }

    private fun setupClickListeners(uuid: String) {
        // Click map: view_id -> category string
        val clickMap = mapOf(
            // General
            "item_gen_all" to "lin_lay_gen_all",
            "item_gen_balance" to "lin_lay_gen_bal",
            "item_gen_fuliza" to "lin_lay_gen_fuliza",
            "item_gen_received" to "lin_lay_gen_recv",
            "item_gen_sent" to "lin_lay_gen_sent",
            "item_gen_withdraw" to "lin_lay_gen_withdraw",
            "item_gen_wrong_pin" to "lin_lay_gen_wrong_pin",
            "item_gen_unknown" to "lin_lay_gen_unknown",

            // Sent
            "item_sent_all" to "lin_lay_sent_all",
            "item_sent_mpesa" to "lin_lay_sent_mpesa",
            "item_sent_mshwari" to "lin_lay_sent_mshwari",
            "item_sent_lnm" to "lin_lay_sent_lnm",
            "item_sent_mini" to "lin_lay_sent_mini",
            "item_sent_cancel" to "lin_lay_sent_cancel",

            // Received
            "item_rec_all" to "lin_lay_rec_all",
            "item_rec_mpesa" to "lin_lay_rec_mpesa",
            "item_rec_mshwari" to "lin_lay_rec_mshwari",
            "item_rec_ncba" to "lin_lay_rec_ncba",
            "item_rec_im" to "lin_lay_rec_im",
            "item_rec_reversal" to "lin_lay_rec_reversal",
            "item_rec_kcb" to "lin_lay_rec_kcb",

            // Balance
            "item_bal_all" to "lin_lay_bal_all",
            "item_bal_mpesa" to "lin_lay_bal_mpesa",
            "item_bal_mshwari" to "lin_lay_bal_mshwari",
            "item_bal_kcb" to "lin_lay_bal_kcb",

            // Fuliza
            "item_fuliza_all" to "lin_lay_fuliza_all",
            "item_fuliza_opt_in" to "lin_lay_fuliza_opt_in",
            "item_fuliza_opt_out" to "lin_lay_fuliza_opt_out",
            "item_fuliza_limit" to "lin_lay_fuliza_limit",
            "item_fuliza_loan" to "lin_lay_fuliza_loan",
            "item_fuliza_mini" to "lin_lay_fuliza_mini",

            // Errors
            "item_error_all" to "lin_lay_error_all",
            "item_error_pin" to "lin_lay_error_pin",
            "item_error_less" to "lin_lay_error_less",
            "item_error_receiver" to "lin_lay_error_receiver",
            "item_error_org" to "lin_lay_error_org",
            "item_error_failed" to "lin_lay_error_failed"
        )

        clickMap.forEach { (viewId, category) ->
            val view = requireView().findViewById<View>(resources.getIdentifier(viewId, "id", requireContext().packageName))
            if (view != null) {
                view.setOnClickListener { getSmsListingFor(category) }
            }
        }

        // Delete button
        btnDeleteSummary.setOnClickListener { getSmsListingFor("Delete Loot") }
    }

    private fun getSmsListingFor(category: String) {
        if (category.isNotEmpty()) {
            val bundle = Bundle()
            bundle.putString("filter_category", category)

            requireActivity().findNavController(R.id.nav_host_fragment_activity_bottom)
                .navigate(R.id.navi_transactions, bundle)
        } else {
            Toast.makeText(activity, "Null Category", Toast.LENGTH_SHORT).show()
        }
    }

    private fun backtoHistory() {
        val navController = requireActivity().findNavController(R.id.nav_host_fragment_activity_bottom)
        navController.popBackStack(R.id.navi_history, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> backtoHistory()
        }
        return super.onOptionsItemSelected(item)
    }
}