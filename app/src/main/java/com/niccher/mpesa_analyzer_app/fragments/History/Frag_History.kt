package com.niccher.mpesa_analyzer_app.fragments.History

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
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.adapter.Adapter_Frag_History
import com.niccher.mpesa_analyzer_app.databinding.FragHistoryBinding
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.helpers.SummaryResponse
import com.niccher.mpesa_analyzer_app.interfaces.JsonProcesses
import com.niccher.mpesa_analyzer_app.konstants.Konstants
import com.niccher.mpesa_analyzer_app.models.Mod_Summaries
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

import android.app.AlertDialog
import android.os.Environment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Frag_History : Fragment() {

    private var _binding: FragHistoryBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var connState: TextView
    private lateinit var connWait: android.widget.LinearLayout
    private lateinit var fabExport: ExtendedFloatingActionButton

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

        fabExport = fragHistory.findViewById(R.id.fab_export)
        fabExport.setOnClickListener {
            if (summariesList.isNotEmpty()) {
                showExportDialog()
            } else {
                Toast.makeText(requireContext(), "No data to export", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun showExportDialog() {
        val options = arrayOf("Export to CSV", "Export to PDF")
        AlertDialog.Builder(requireContext())
            .setTitle("Export History")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportToCsv()
                    1 -> exportToPdf()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportToCsv() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "MPesa_History_$timestamp.csv")
            val fos = FileOutputStream(file)
            val writer = OutputStreamWriter(fos)

            writer.append("Date,Sent,Received,Total Interactions,Reference UUID\n")
            for (summary in summariesList) {
                // Formatting fields minimally and avoiding potential CSV conflicts by using basic values
                writer.append("${summary.summary_Created},${summary.summary_Sent},${summary.summary_Received},${summary.summary_Count},${summary.summary_Loot_Uuid}\n")
            }

            writer.close()
            fos.close()
            Toast.makeText(requireContext(), "Exported to Downloads folder", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to export CSV: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(kon.TAGGED, "exportToCsv: ${e.message}", e)
        }
    }

    private fun exportToPdf() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "MPesa_History_$timestamp.pdf")

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            document.add(Paragraph("M-Pesa Analyzer - Transaction History\n\n"))

            val table = PdfPTable(4)
            table.widthPercentage = 100f

            // Add Headers
            val headers = arrayOf("Date", "Sent", "Received", "Interactions")
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }

            // Add Data
            for (summary in summariesList) {
                table.addCell(summary.summary_Created)
                table.addCell(summary.summary_Sent)
                table.addCell(summary.summary_Received)
                table.addCell(summary.summary_Count)
            }

            document.add(table)
            document.close()

            Toast.makeText(requireContext(), "Exported to Downloads folder as PDF", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(kon.TAGGED, "exportToPdf: ${e.message}", e)
        }
    }

}