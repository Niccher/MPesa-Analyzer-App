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
import com.niccher.mpesa_analyzer.helpers.ServiceGenerators
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.adapter.Adapter_Frag_History
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
    private var gson: Gson? = null

    private lateinit var tvGenDate: TextView
    private lateinit var tvGenTime: TextView
    private lateinit var tvTotalInteractions: TextView

    private lateinit var tvQuickCategories: TextView
    private lateinit var tvQuickClassified: TextView
    private lateinit var tvQuickUnclassified: TextView
    private lateinit var tvQuickTransactionTypes: TextView

    private lateinit var tvMoneyIn: TextView
    private lateinit var tvMoneyOut: TextView
    private lateinit var tvMoneyUnknown: TextView
    private lateinit var tvBalanceReq: TextView

    private lateinit var llCategoryBreakdown: LinearLayout
    private lateinit var btnDeleteSummary: com.google.android.material.button.MaterialButton
    private lateinit var layoutInteractions: LinearLayout
    private lateinit var progressBar: LinearLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var emptyState: LinearLayout

    private var lootUuid: String = ""
    private var categoryBreakdown: Map<String, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kon = Konstants
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

        tvGenDate = view.findViewById(R.id.cat_point_loot_date)
        tvGenTime = view.findViewById(R.id.cat_point_loot_time)
        tvTotalInteractions = view.findViewById(R.id.tv_total_interactions)

        tvQuickCategories = view.findViewById(R.id.tv_quick_categories)
        tvQuickClassified = view.findViewById(R.id.tv_quick_classified)
        tvQuickUnclassified = view.findViewById(R.id.tv_quick_unclassified)

        tvMoneyIn = view.findViewById(R.id.tv_money_in)
        tvMoneyOut = view.findViewById(R.id.tv_money_out)
        tvMoneyUnknown = view.findViewById(R.id.tv_money_unknown)
        tvBalanceReq = view.findViewById(R.id.tv_balance_req)

        tvQuickTransactionTypes = view.findViewById(R.id.tv_quick_transaction_types)

        llCategoryBreakdown = view.findViewById(R.id.ll_category_breakdown)
        btnDeleteSummary = view.findViewById(R.id.btn_delete_summary)
        layoutInteractions = view.findViewById(R.id.summary_interactions_layout)
        progressBar = view.findViewById(R.id.summary_progress_bar)
        scrollView = view.findViewById(R.id.summary_scroll_view)
        emptyState = view.findViewById(R.id.summary_error_state)

        categoryBreakdown = try {
            val json = arguments?.getString("category_breakdown")
            if (json != null) gson?.fromJson<Map<String, Int>>(json, object : TypeToken<Map<String, Int>>() {}.type) else null
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

    private fun getConnectionState() {
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm.activeNetworkInfo?.isConnected == true) {
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
        val stName = arguments?.getString("summary_loot_name")
        if (!stName.isNullOrEmpty()) getSummaries(stName)
    }

    private fun getSummaries(lootName: String) {
        val params = HashMap<String, String>().apply {
            put("varUser", pref.getPrefsAuth("auth", requireContext()))
            put("varDev", pref.getPrefsAuth("print", requireActivity()))
            put("varLootUuid", lootName)
        }

        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        layoutInteractions.visibility = View.GONE
        emptyState.visibility = View.GONE

        ServiceGenerators.createService(JsonProcesses::class.java, requireContext())
            .getSummaryCalc(params)
            .enqueue(object :  retrofit2.Callback<Mod_Loot_Summary> {
                override fun onResponse(call:  retrofit2.Call<Mod_Loot_Summary>, response:  retrofit2.Response<Mod_Loot_Summary>) {
                    if (response.isSuccessful && response.body() != null) {
                        progressBar.visibility = View.GONE
                        scrollView.visibility = View.VISIBLE
                        layoutInteractions.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE

                        val s = response.body()!!.loot_summarizer
                        lootUuid = s.loot_Uuid

                        setDateTime(s.loot_Created)
                        tvTotalInteractions.text = "${s.count_All} Interactions"

                        val classified = s.count_All - s.count_Unknown
                        val unclassified = s.count_Unknown

                        tvQuickClassified.text = classified.toString()
                        tvQuickUnclassified.text = unclassified.toString()

                        val catsCount = categoryBreakdown?.count { it.value > 0 } ?: 0
                        tvQuickCategories.text = catsCount.toString()

                        val dir = s.direction_breakdown ?: emptyMap()
                        tvMoneyIn.text = (dir["Money In"] ?: 0).toString()
                        tvMoneyOut.text = (dir["Money Out"] ?: 0).toString()
                        tvMoneyUnknown.text = (dir["Unknown"] ?: 0).toString()
                        tvBalanceReq.text = (dir["Balance Inquiry"] ?: 0).toString()

                        val typeCount = s.transaction_type_breakdown?.count { it.value > 0 } ?: 0
                        tvQuickTransactionTypes.text = typeCount.toString()

                        populateCategoryBreakdown()
                    }
                }

                override fun onFailure(call:  retrofit2.Call<Mod_Loot_Summary>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showOfflineState()
                    Log.e(kon.TAGGED, "Summary fetch failed: ${t.message}")
                }
            })
    }

    private fun setDateTime(raw: String) {
        try {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val d = fmt.parse(raw)
            if (d != null) {
                tvGenDate.text = java.text.SimpleDateFormat("EEEE, MMM dd", java.util.Locale.getDefault()).format(d)
                tvGenTime.text = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(d)
            } else {
                val parts = raw.split(" ")
                tvGenDate.text = parts[0]
                tvGenTime.text = parts.getOrNull(1) ?: ""
            }
        } catch (_: Exception) {
            val parts = raw.split(" ")
            tvGenDate.text = parts[0]
            tvGenTime.text = parts.getOrNull(1) ?: ""
        }
    }

    private fun populateCategoryBreakdown() {
        llCategoryBreakdown.removeAllViews()
        val breakdown = categoryBreakdown?.filter { it.value > 0 } ?: run {
            llCategoryBreakdown.visibility = View.GONE
            return
        }

        val cats = Adapter_Frag_History.CATEGORIES
            .filter { breakdown.containsKey(it.key) }
            .sortedByDescending { breakdown[it.key] }

        if (cats.isEmpty()) {
            llCategoryBreakdown.visibility = View.GONE
            return
        }

        var currentRow: LinearLayout? = null

        for ((i, cat) in cats.withIndex()) {
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

            val count = breakdown[cat.key] ?: 0
            val badge = LayoutInflater.from(requireContext()).inflate(R.layout.badge_category, currentRow, false) as LinearLayout
            badge.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val icon = badge.findViewById<ImageView>(R.id.badge_icon)
            val label = badge.findViewById<TextView>(R.id.badge_label)
            val countTv = badge.findViewById<TextView>(R.id.badge_count)

            val color = androidx.core.content.ContextCompat.getColor(requireContext(), cat.colorRes)
            icon.setImageResource(cat.iconRes)
            icon.setColorFilter(color)
            label.text = cat.label
            label.setTextColor(color)
            countTv.text = count.toString()
            countTv.setTextColor(color)

            currentRow?.addView(badge)
        }
    }

    private fun backtoHistory() {
        requireActivity().findNavController(R.id.nav_host_fragment_activity_bottom)
            .popBackStack(R.id.navi_history, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) backtoHistory()
        return super.onOptionsItemSelected(item)
    }
}
