package com.niccher.mpesa_analyzer_app.fragments.History

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.adapter.TransactionsAdapter
import com.niccher.mpesa_analyzer_app.models.TransactionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class TransactionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionsAdapter
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var btnDateFilter: Button
    private lateinit var txtEmpty: TextView
    private lateinit var progressLoading: android.widget.LinearLayout

    private var allTransactions = ArrayList<TransactionModel>()
    private var currentFilterType = "all"
    private var startDate: Long = 0
    private var endDate: Long = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.frag_transactions, container, false)

        recyclerView = root.findViewById(R.id.recy_transactions)
        searchView = root.findViewById(R.id.search_transactions)
        chipGroup = root.findViewById(R.id.chip_group_filters)
        btnDateFilter = root.findViewById<Button>(R.id.btn_date_filter)
        txtEmpty = root.findViewById<TextView>(R.id.txt_empty)
        progressLoading = root.findViewById<android.widget.LinearLayout>(R.id.progress_loading)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TransactionsAdapter(ArrayList())
        recyclerView.adapter = adapter

        setupSearch()
        setupFilters()
        setupDatePicker()

        val initialCategory = arguments?.getString("filter_category")
        if (!initialCategory.isNullOrEmpty()) {
            setInitialFilter(initialCategory)
        }

        loadTransactions()

        return root
    }

    private fun setInitialFilter(category: String) {
        when {
            category.contains("sent", true) -> {
                chipGroup.check(R.id.chip_sent)
                currentFilterType = "sent"
            }
            category.contains("receive", true) || category.contains("get", true) -> {
                chipGroup.check(R.id.chip_received)
                currentFilterType = "received"
            }
            category.contains("paybill", true) -> {
                chipGroup.check(R.id.chip_paybill)
                currentFilterType = "paybill"
            }
            category.contains("withdraw", true) -> {
                chipGroup.check(R.id.chip_withdraw)
                currentFilterType = "withdraw"
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun setupFilters() {
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilterType = when (checkedId) {
                R.id.chip_sent -> "sent"
                R.id.chip_received -> "received"
                R.id.chip_paybill -> "paybill"
                R.id.chip_withdraw -> "withdraw"
                else -> "all"
            }
            applyFilters()
        }
    }

    private fun setupDatePicker() {
        btnDateFilter.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                startDate = selection.first
                endDate = selection.second
                btnDateFilter.text = "Filtered by Date"
                applyFilters()
            }
            dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
        }
    }

    private fun loadTransactions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        lifecycleScope.launch {
            progressLoading.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            txtEmpty.visibility = View.GONE

            val transactions = withContext(Dispatchers.IO) {
                val list = ArrayList<TransactionModel>()
                val cr = requireContext().contentResolver
                val cursor = cr.query(Telephony.Sms.CONTENT_URI, null, "address = ?", arrayOf("MPESA"), "date DESC")

                cursor?.use {
                    val bodyIdx = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                    val dateIdx = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
                    val idIdx = it.getColumnIndexOrThrow(Telephony.Sms._ID)
                    val addrIdx = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)

                    while (it.moveToNext()) {
                        val body = it.getString(bodyIdx)
                        val trans = TransactionModel(
                            id = it.getString(idIdx),
                            date = it.getLong(dateIdx),
                            address = it.getString(addrIdx),
                            body = body
                        )
                        parseMpesaSms(trans)
                        list.add(trans)
                    }
                }
                list
            }

            allTransactions.clear()
            allTransactions.addAll(transactions)
            
            progressLoading.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            
            applyFilters()
        }
    }

    private fun parseMpesaSms(trans: TransactionModel) {
        val b = trans.body
        when {
            b.contains("sent to", true) || b.contains("paid to", true) -> {
                trans.type = if (b.contains("paybill", true)) "Paybill" else "Sent"
                trans.amount = extractAmount(b)
                trans.name = extractName(b, "to")
                trans.reference = extractRef(b)
            }
            b.contains("received", true) -> {
                trans.type = "Received"
                trans.amount = extractAmount(b)
                trans.name = extractName(b, "from")
                trans.reference = extractRef(b)
            }
            b.contains("withdraw", true) -> {
                trans.type = "Withdraw"
                trans.amount = extractAmount(b)
                trans.reference = extractRef(b)
            }
        }
    }

    private fun extractAmount(body: String): Double {
        // Match Ksh followed by digits and commas, with an optional decimal part
        val pattern = Pattern.compile("Ksh\\s*([0-9,]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            return try {
                matcher.group(1).replace(",", "").toDouble()
            } catch (e: Exception) {
                0.0
            }
        }
        return 0.0
    }

    private fun extractName(body: String, prefix: String): String {
        val pattern = Pattern.compile("$prefix\\s+([^.]+)\\.", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(body)
        return if (matcher.find()) matcher.group(1).trim() else ""
    }

    private fun extractRef(body: String): String {
        return body.split(" ")[0] // Usually the first word is the reference code
    }

    private fun applyFilters() {
        var filtered = allTransactions.filter { 
            (currentFilterType == "all" || it.type.lowercase() == currentFilterType) &&
            (it.date in startDate..endDate || startDate == 0L)
        }
        
        adapter.updateList(filtered)
        txtEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
