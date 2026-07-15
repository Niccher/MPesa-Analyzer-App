package com.niccher.mpesa_analyzer_app.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.fragments.History.Frag_Summary
import com.niccher.mpesa_analyzer_app.models.Mod_Summaries
import java.text.SimpleDateFormat
import java.util.*

class Adapter_Frag_History(
    private val summariesList: ArrayList<Mod_Summaries>,
    private val context: Context
) : RecyclerView.Adapter<Adapter_Frag_History.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val part_date: TextView = itemView.findViewById(R.id.part_txt_date)
        val part_sent: TextView = itemView.findViewById(R.id.part_txt_sent)
        val part_unknown: TextView = itemView.findViewById(R.id.part_txt_unknown)
        val part_receive: TextView = itemView.findViewById(R.id.part_txt_incoming)
        val part_count: TextView = itemView.findViewById(R.id.part_txt_total)
        val part_frame: androidx.cardview.widget.CardView = itemView.findViewById(R.id.part_txt_frame)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.part_posted, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val summary = summariesList[position]

        // Handle both Unix timestamp and formatted date string
        val dateStr = summary.summary_Created
        try {
            val timestamp = dateStr.toLongOrNull()
            val formattedDate = if (timestamp != null) {
                if (timestamp > 1000000000000L) {
                    // Milliseconds timestamp
                    SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault()).format(timestamp)
                } else if (timestamp > 1000000000L) {
                    // Seconds timestamp
                    SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault()).format(timestamp * 1000)
                } else {
                    dateStr
                }
            } else {
                // Already formatted date string
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault())
                outputFormat.format(inputFormat.parse(dateStr))
            }
            holder.part_date.text = formattedDate
        } catch (ex: Exception) {
            holder.part_date.text = dateStr
        }

        holder.part_sent.text = summary.summary_Sent
        holder.part_unknown.text = summary.summary_Unknown
        holder.part_receive.text = summary.summary_Received
        holder.part_count.text = "Interactions: ${summary.summary_Count}"

        holder.part_frame.setOnClickListener {
            val activity = context as AppCompatActivity
            val fragSummary = Frag_Summary()

            val bundle = Bundle()
            bundle.putString("summary_loot_name", summary.summary_Loot_Uuid)
            fragSummary.arguments = bundle

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_bottom, fragSummary)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return summariesList.size
    }
}