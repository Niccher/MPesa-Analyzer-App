package com.niccher.my_mpesa_analyzer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.niccher.my_mpesa_analyzer.R
import com.niccher.my_mpesa_analyzer.models.Mod_Summaries
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
        val part_frame: ConstraintLayout = itemView.findViewById(R.id.part_txt_frame)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.part_posted, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val summary = summariesList[position]

        val format = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        try {
            val dateTime = format.parse(summary.summary_Created)
            val dateFormat = SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault())
            val dated = dateFormat.format(dateTime)
            holder.part_date.text = dated
        } catch (ex: Exception) {
            holder.part_date.text = summary.summary_Created
        }

        holder.part_sent.text = summary.summary_Sent
        holder.part_unknown.text = summary.summary_Unknown
        holder.part_receive.text = summary.summary_Received
        holder.part_count.text = "Interactions: ${summary.summary_Count}"

        holder.part_frame.setOnClickListener {
//            Toast.makeText(context, "Pressed", Toast.LENGTH_LONG)
//            val activity = context as AppCompatActivity
//            val fragSummary = Frag_Summary()
//
//            val bundle = Bundle()
//            bundle.putString("summary_loot_name", summary.summary_Loot_Uuid)
//            fragSummary.arguments = bundle
//
//            activity.supportFragmentManager.beginTransaction()
//                .replace(R.id.frame, fragSummary)
//                .addToBackStack(null)
//                .commit()
        }
    }

    override fun getItemCount(): Int {
        return summariesList.size
    }
}