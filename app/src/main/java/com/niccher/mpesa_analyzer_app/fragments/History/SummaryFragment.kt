package com.niccher.mpesa_analyzer_app.fragments.History

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.adapter.HistoryAdapter
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.api.ProcessesApiService
import com.niccher.mpesa_analyzer_app.constants.Constants
import com.niccher.mpesa_analyzer_app.models.LootSummarizer
import com.niccher.mpesa_analyzer_app.models.LootSummaryModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class SummaryFragment : Fragment() {

    companion object {
        data class CatMeta(val key: String, val label: String, val colorRes: Int, val iconRes: Int)
        data class TypeMeta(val key: String, val label: String, val colorRes: Int, val iconRes: Int)

        val CATEGORIES = listOf(
            CatMeta("Mobile Money",   "Mobile Money",   R.color.cat_mobile_money,     R.drawable.ic_cat_mobile_money),
            CatMeta("Bank",           "Bank",           R.color.cat_bank,             R.drawable.ic_cat_bank),
            CatMeta("Fintech",        "Fintech",        R.color.cat_fintech,          R.drawable.ic_cat_fintech),
            CatMeta("SACCO",          "SACCO",          R.color.cat_sacco,            R.drawable.ic_cat_sacco),
            CatMeta("Insurance",      "Insurance",      R.color.cat_insurance,        R.drawable.ic_cat_insurance),
            CatMeta("Payments/Govt",  "Payments/Govt",  R.color.cat_payments_govt,    R.drawable.ic_cat_payments_govt),
            CatMeta("Other Finance",  "Other Finance",  R.color.cat_other_finance,    R.drawable.ic_cat_other_finance),
            CatMeta("Non-Finance",    "Non-Finance",    R.color.cat_non_finance,      R.drawable.ic_cat_non_finance),
            CatMeta("Unclassified",   "Unclassified",   R.color.cat_unclassified,     R.drawable.ic_cat_unclassified),
        )

        val TYPES = listOf(
            TypeMeta("peer_to_peer",  "Peer to Peer",         R.color.brand_primary,        R.drawable.ic_person),
            TypeMeta("payment",       "Payments / Purchases", R.color.cat_payments_govt,    R.drawable.ic_cat_payments_govt),
            TypeMeta("loan",          "Loans & Fuliza",       R.color.semantic_danger,      R.drawable.ic_fuliza),
            TypeMeta("deposit",       "Deposits",             R.color.semantic_success,     R.drawable.ic_received),
            TypeMeta("withdrawal",    "Withdrawals",          R.color.cat_other_finance,    R.drawable.ic_cat_other_finance),
            TypeMeta("reversal",      "Reversals",            R.color.semantic_warning,     R.drawable.ic_balance),
            TypeMeta("balance",       "Balance Inquiries",    R.color.text_secondary,       R.drawable.ic_balance),
            TypeMeta("unknown",       "Others / Unknown",     R.color.text_muted,           R.drawable.ic_info),
        )
    }

    // Views
    private lateinit var kon: Constants
    private lateinit var activity: AppCompatActivity
    private lateinit var pref: Prefs
    private var gson: Gson? = null

    private lateinit var tvGenDate: TextView
    private lateinit var tvGenTime: TextView
    private lateinit var tvTotalInteractions: TextView

    private lateinit var tvSummaryVolume: TextView
    private lateinit var tvSummarySenders: TextView

    private lateinit var tvQuickCategories: TextView
    private lateinit var tvQuickClassified: TextView
    private lateinit var tvQuickUnclassified: TextView
    private lateinit var tvQuickTransactionTypes: TextView

    private lateinit var tvMoneyIn: TextView
    private lateinit var tvMoneyOut: TextView
    private lateinit var tvMoneyUnknown: TextView
    private lateinit var tvBalanceReq: TextView

    private lateinit var llTypeBreakdown: LinearLayout
    private lateinit var llCategoryBreakdown: LinearLayout

    private lateinit var btnDeleteSummary: com.google.android.material.button.MaterialButton
    private lateinit var layoutInteractions: LinearLayout
    private lateinit var progressBar: LinearLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var emptyState: LinearLayout

    private var lootUuid: String = ""
    private var categoryBreakdown: Map<String, Int>? = null

    private lateinit var tvInflowLabel: TextView
    private lateinit var tvOutflowLabel: TextView
    private lateinit var pbFlowRatio: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var cardCounterparties: androidx.cardview.widget.CardView
    private lateinit var llCounterpartiesList: LinearLayout

    // ─────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kon = Constants
        pref = Prefs()
        gson = GsonBuilder().setLenient().create()
        activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.apply {
            title = "Summary Info"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.frag_summary, container, false)

        // Header
        tvGenDate            = view.findViewById(R.id.cat_point_loot_date)
        tvGenTime            = view.findViewById(R.id.cat_point_loot_time)
        tvTotalInteractions  = view.findViewById(R.id.tv_total_interactions)

        // Financial highlights
        tvSummaryVolume      = view.findViewById(R.id.tv_summary_volume)
        tvSummarySenders     = view.findViewById(R.id.tv_summary_senders)

        // Quick stats
        tvQuickCategories    = view.findViewById(R.id.tv_quick_categories)
        tvQuickClassified    = view.findViewById(R.id.tv_quick_classified)
        tvQuickUnclassified  = view.findViewById(R.id.tv_quick_unclassified)
        tvQuickTransactionTypes = view.findViewById(R.id.tv_quick_transaction_types)

        // Money flow
        tvMoneyIn            = view.findViewById(R.id.tv_money_in)
        tvMoneyOut           = view.findViewById(R.id.tv_money_out)
        tvMoneyUnknown       = view.findViewById(R.id.tv_money_unknown)
        tvBalanceReq         = view.findViewById(R.id.tv_balance_req)

        // Dynamic breakdown containers
        llTypeBreakdown      = view.findViewById(R.id.ll_type_breakdown)
        llCategoryBreakdown  = view.findViewById(R.id.ll_category_breakdown)

        // Chrome
        btnDeleteSummary     = view.findViewById(R.id.btn_delete_summary)
        layoutInteractions   = view.findViewById(R.id.summary_interactions_layout)
        progressBar          = view.findViewById(R.id.summary_progress_bar)
        scrollView           = view.findViewById(R.id.summary_scroll_view)
        emptyState           = view.findViewById(R.id.summary_error_state)

        tvInflowLabel        = view.findViewById(R.id.tv_inflow_label)
        tvOutflowLabel       = view.findViewById(R.id.tv_outflow_label)
        pbFlowRatio          = view.findViewById(R.id.pb_flow_ratio)
        cardCounterparties   = view.findViewById(R.id.card_counterparties)
        llCounterpartiesList = view.findViewById(R.id.ll_counterparties_list)

        // Pre-populate category breakdown from bundle (History adapter passes it)
        categoryBreakdown = try {
            val json = arguments?.getString("category_breakdown")
            if (json != null) gson?.fromJson(json, object : TypeToken<Map<String, Int>>() {}.type) else null
        } catch (e: Exception) { null }

        btnDeleteSummary.setOnClickListener {
            Toast.makeText(requireContext(), "Delete not implemented", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<TextView>(R.id.tv_retry).setOnClickListener { getConnectionState() }

        getConnectionState()
        return view
    }

    override fun onResume() {
        super.onResume()
        getConnectionState()
    }

    // ─────────────────────────────────────────────────────────────
    private fun getConnectionState() {
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm.activeNetworkInfo?.isConnected == true) getReferences() else showOfflineState()
    }

    private fun showOfflineState() {
        progressBar.visibility  = View.GONE
        scrollView.visibility   = View.GONE
        layoutInteractions.visibility = View.GONE
        emptyState.visibility   = View.VISIBLE
    }

    private fun getReferences() {
        val stName = arguments?.getString("summary_loot_name")
        if (!stName.isNullOrEmpty()) getSummaries(stName)
    }

    private fun getSummaries(lootName: String) {
        val params = HashMap<String, String>().apply {
            put("varUser", pref.getPrefsAuth("auth", requireContext()))
            put("varDev",  pref.getPrefsAuth("print", requireActivity()))
            put("varLootUuid", lootName)
        }

        progressBar.visibility  = View.VISIBLE
        scrollView.visibility   = View.GONE
        layoutInteractions.visibility = View.GONE
        emptyState.visibility   = View.GONE

        ServiceGenerator.createService(ProcessesApiService::class.java, requireContext())
            .getSummaryCalc(params)
            .enqueue(object : Callback<LootSummaryModel> {
                override fun onResponse(call: Call<LootSummaryModel>, response: Response<LootSummaryModel>) {
                    if (response.isSuccessful && response.body() != null) {
                        progressBar.visibility  = View.GONE
                        scrollView.visibility   = View.VISIBLE
                        layoutInteractions.visibility = View.VISIBLE
                        emptyState.visibility   = View.GONE

                        bindSummary(response.body()!!.loot_summarizer)
                    } else {
                        progressBar.visibility = View.GONE
                        showOfflineState()
                    }
                }

                override fun onFailure(call: Call<LootSummaryModel>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showOfflineState()
                    Log.e(kon.TAGGED, "Summary fetch failed: ${t.message}")
                }
            })
    }

    // ─────────────────────────────────────────────────────────────
    private fun bindSummary(s: LootSummarizer) {
        lootUuid = s.loot_Uuid

        // 1. Header
        setDateTime(s.loot_Created)
        tvTotalInteractions.text = "${s.count_All} SMS"

        // 2. Financial highlights
        val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        tvSummaryVolume.text  = "Ksh ${fmt.format(s.total_amount)}"
        tvSummarySenders.text = s.finance_senders.toString()

        // 3. Quick stats
        val classified   = s.count_All - s.count_Unknown
        tvQuickClassified.text   = classified.toString()
        tvQuickUnclassified.text = s.count_Unknown.toString()

        if (categoryBreakdown.isNullOrEmpty()) categoryBreakdown = s.category_breakdown
        tvQuickCategories.text = (categoryBreakdown?.count { it.value > 0 } ?: 0).toString()

        val typeCount = s.transaction_type_breakdown?.count { it.value > 0 } ?: 0
        tvQuickTransactionTypes.text = typeCount.toString()

        // 4. Money flow
        val dir = s.direction_breakdown ?: emptyMap()
        val inflowCount = dir["Money In"] ?: 0
        val outflowCount = dir["Money Out"] ?: 0
        tvMoneyIn.text      = inflowCount.toString()
        tvMoneyOut.text    = outflowCount.toString()
        tvMoneyUnknown.text = (dir["Unknown"]          ?: 0).toString()
        tvBalanceReq.text   = (dir["Balance Inquiry"]  ?: 0).toString()

        val totalFlow = inflowCount + outflowCount
        if (totalFlow > 0) {
            val inflowPercent = (inflowCount * 100) / totalFlow
            val outflowPercent = 100 - inflowPercent
            tvInflowLabel.text = "Inflow: $inflowPercent%"
            tvOutflowLabel.text = "Outflow: $outflowPercent%"
            pbFlowRatio.progress = inflowPercent
        } else {
            tvInflowLabel.text = "Inflow: 0%"
            tvOutflowLabel.text = "Outflow: 0%"
            pbFlowRatio.progress = 50
        }

        // 5. Counterparties (LLM)
        populateCounterparties(s)

        // 6. Transaction types (LLM)
        populateTypeBreakdown(s)

        // 7. Categories (LLM)
        populateCategoryBreakdown()
    }

    // ─────────────────────────────────────────────────────────────
    private fun populateTypeBreakdown(s: LootSummarizer) {
        llTypeBreakdown.removeAllViews()
        val breakdown = s.transaction_type_breakdown?.filter { it.value > 0 }
        if (breakdown.isNullOrEmpty()) {
            llTypeBreakdown.visibility = View.GONE
            return
        }
        llTypeBreakdown.visibility = View.VISIBLE

        val sorted = TYPES.filter { breakdown.containsKey(it.key) }
            .sortedByDescending { breakdown[it.key] }

        sorted.forEachIndexed { index, type ->
            // Divider between rows
            if (index > 0) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).also { it.setMargins(0, 0, 0, 0) }
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_ui_divider))
                }
                llTypeBreakdown.addView(divider)
            }

            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_type_row, llTypeBreakdown, false)

            val count = breakdown[type.key] ?: 0
            val color = ContextCompat.getColor(requireContext(), type.colorRes)

            row.findViewById<ImageView>(R.id.type_row_icon).apply {
                setImageResource(type.iconRes)
                setColorFilter(color)
            }
            row.findViewById<TextView>(R.id.type_row_label).apply {
                text = type.label
                setTextColor(color)
            }
            row.findViewById<TextView>(R.id.type_row_count).apply {
                text = count.toString()
                setTextColor(color)
            }
            llTypeBreakdown.addView(row)
        }

        // Also add any LLM-returned keys that aren't in our TYPES list
        val knownKeys = TYPES.map { it.key }.toSet()
        breakdown.filter { it.key !in knownKeys }
            .entries.sortedByDescending { it.value }
            .forEach { (key, count) ->
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_ui_divider))
                }
                llTypeBreakdown.addView(divider)

                val row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_type_row, llTypeBreakdown, false)
                val color = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                row.findViewById<ImageView>(R.id.type_row_icon).apply {
                    setImageResource(R.drawable.ic_info)
                    setColorFilter(color)
                }
                row.findViewById<TextView>(R.id.type_row_label).apply {
                    text = key.replace("_", " ").replaceFirstChar { it.uppercase() }
                    setTextColor(color)
                }
                row.findViewById<TextView>(R.id.type_row_count).apply {
                    text = count.toString()
                    setTextColor(color)
                }
                llTypeBreakdown.addView(row)
            }
    }

    // ─────────────────────────────────────────────────────────────
    private fun populateCounterparties(s: LootSummarizer) {
        llCounterpartiesList.removeAllViews()
        val list = s.top_counterparties
        if (list.isNullOrEmpty()) {
            cardCounterparties.visibility = View.GONE
            return
        }
        cardCounterparties.visibility = View.VISIBLE

        val fmt = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }

        list.forEachIndexed { index, cp ->
            if (index > 0) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_ui_divider))
                }
                llCounterpartiesList.addView(divider)
            }

            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_type_row, llCounterpartiesList, false)

            row.findViewById<ImageView>(R.id.type_row_icon).apply {
                setImageResource(R.drawable.ic_person)
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
            row.findViewById<TextView>(R.id.type_row_label).apply {
                text = cp.counterparty
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }
            row.findViewById<TextView>(R.id.type_row_count).apply {
                text = "Ksh ${fmt.format(cp.total_amount)}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_primary))
            }
            llCounterpartiesList.addView(row)
        }
    }

    // ─────────────────────────────────────────────────────────────
    private fun populateCategoryBreakdown() {
        llCategoryBreakdown.removeAllViews()
        val breakdown = categoryBreakdown?.filter { it.value > 0 }
        if (breakdown.isNullOrEmpty()) {
            llCategoryBreakdown.visibility = View.GONE
            return
        }
        llCategoryBreakdown.visibility = View.VISIBLE

        val cats = CATEGORIES.filter { breakdown.containsKey(it.key) }
        var currentRow: LinearLayout? = null

        cats.forEachIndexed { i, cat ->
            if (i % 3 == 0) {
                currentRow = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                }
                llCategoryBreakdown.addView(currentRow)
            }

            val count  = breakdown[cat.key] ?: 0
            val badge  = LayoutInflater.from(requireContext())
                .inflate(R.layout.badge_category, currentRow, false) as LinearLayout
            badge.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val color = ContextCompat.getColor(requireContext(), cat.colorRes)
            badge.findViewById<ImageView>(R.id.badge_icon).apply {
                setImageResource(cat.iconRes)
                setColorFilter(color)
            }
            badge.findViewById<TextView>(R.id.badge_label).apply {
                text = cat.label
                setTextColor(color)
            }
            badge.findViewById<TextView>(R.id.badge_count).apply {
                text = count.toString()
                setTextColor(color)
            }
            currentRow?.addView(badge)
        }
    }

    // ─────────────────────────────────────────────────────────────
    private fun setDateTime(raw: String) {
        try {
            val timestamp = raw.toLongOrNull()
            val date: java.util.Date? = when {
                timestamp != null && timestamp > 1_000_000_000_000L -> java.util.Date(timestamp)
                timestamp != null && timestamp > 1_000_000_000L     -> java.util.Date(timestamp * 1000)
                else -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(raw)
            }
            if (date != null) {
                tvGenDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                tvGenTime.text = SimpleDateFormat("HH:mm:ss",      Locale.getDefault()).format(date)
            } else {
                tvGenDate.text = raw; tvGenTime.text = ""
            }
        } catch (e: Exception) {
            tvGenDate.text = raw; tvGenTime.text = ""
        }
    }

    // ─────────────────────────────────────────────────────────────
    private fun backtoHistory() {
        requireActivity().findNavController(R.id.nav_host_fragment_activity_bottom)
            .popBackStack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            backtoHistory()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
