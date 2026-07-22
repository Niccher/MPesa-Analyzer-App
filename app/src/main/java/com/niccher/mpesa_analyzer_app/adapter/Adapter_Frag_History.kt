package com.niccher.mpesa_analyzer_app.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
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
        val part_frame: CardView = itemView.findViewById(R.id.part_txt_frame)
        val categoryBadges: LinearLayout = itemView.findViewById(R.id.category_badges)
    }

    companion object {
        data class CatMeta(val key: String, val label: String, val colorRes: Int, val iconRes: Int)
        val CATEGORIES = listOf(
            CatMeta("Mobile Money", "Mobile Money", R.color.cat_mobile_money, R.drawable.ic_cat_mobile_money),
            CatMeta("Bank", "Bank", R.color.cat_bank, R.drawable.ic_cat_bank),
            CatMeta("Fintech", "Fintech", R.color.cat_fintech, R.drawable.ic_cat_fintech),
            CatMeta("SACCO", "SACCO", R.color.cat_sacco, R.drawable.ic_cat_sacco),
            CatMeta("Insurance", "Insurance", R.color.cat_insurance, R.drawable.ic_cat_insurance),
            CatMeta("Payments/Govt", "Payments/Govt", R.color.cat_payments_govt, R.drawable.ic_cat_payments_govt),
            CatMeta("Other Finance", "Other Finance", R.color.cat_other_finance, R.drawable.ic_cat_other_finance),
            CatMeta("Non-Finance", "Non-Finance", R.color.cat_non_finance, R.drawable.ic_cat_non_finance),
            CatMeta("Unclassified", "Unclassified", R.color.cat_unclassified, R.drawable.ic_cat_unclassified),
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.part_posted, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val summary = summariesList[position]

        val dateStr = summary.summary_Created
        try {
            val timestamp = dateStr.toLongOrNull()
            val formattedDate = if (timestamp != null) {
                if (timestamp > 1000000000000L) {
                    SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault()).format(timestamp)
                } else if (timestamp > 1000000000L) {
                    SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault()).format(timestamp * 1000)
                } else {
                    dateStr
                }
            } else {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault())
                outputFormat.format(inputFormat.parse(dateStr))
            }
            holder.part_date.text = formattedDate
        } catch (ex: Exception) {
            holder.part_date.text = dateStr
        }

        holder.part_sent.text = summary.summary_Sent.toString()
        holder.part_unknown.text = summary.summary_Unknown.toString()
        holder.part_receive.text = summary.summary_Received.toString()
        holder.part_count.text = "Interactions: ${summary.summary_Count}"

        buildCategoryBadges(holder.categoryBadges, summary)

        holder.part_frame.setOnClickListener {
            val activity = context as AppCompatActivity
            val fragSummary = Frag_Summary()

            val bundle = Bundle()
            bundle.putString("summary_loot_name", summary.summary_Loot_Uuid)
            if (summary.category_breakdown != null) {
                bundle.putString("category_breakdown", Gson().toJson(summary.category_breakdown))
            }
            fragSummary.arguments = bundle

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_bottom, fragSummary)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun buildCategoryBadges(container: LinearLayout, summary: Mod_Summaries) {
        container.removeAllViews()
        val breakdown = summary.category_breakdown ?: return
        val density = context.resources.displayMetrics.density

        val cats = CATEGORIES.filter { breakdown.containsKey(it.key) && (breakdown[it.key] ?: 0) > 0 }
            .sortedByDescending { breakdown[it.key] }

        if (cats.isEmpty()) return

        // Row layout: horizontal LinearLayout that wraps
        var currentRow: LinearLayout? = null
        val rowParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val itemParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ).apply { setMargins(0, 0, 0, (6 * density).toInt()) }

        for ((i, cat) in cats.withIndex()) {
            // Every 3 items start a new row
            if (i % 3 == 0) {
                currentRow = LinearLayout(context).apply {
                    layoutParams = rowParams
                    orientation = LinearLayout.HORIZONTAL
                }
                container.addView(currentRow)
            }

            val count = breakdown[cat.key] ?: 0
            val badge = LayoutInflater.from(context).inflate(R.layout.badge_category, currentRow, false) as LinearLayout
            badge.layoutParams = itemParams

            val icon = badge.findViewById<ImageView>(R.id.badge_icon)
            val label = badge.findViewById<TextView>(R.id.badge_label)
            val countTv = badge.findViewById<TextView>(R.id.badge_count)

            val color = ContextCompat.getColor(context, cat.colorRes)
            icon.setImageResource(cat.iconRes)
            icon.setColorFilter(color)
            label.text = cat.label
            label.setTextColor(color)
            countTv.text = count.toString()
            countTv.setTextColor(color)

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0)
            }

            currentRow?.addView(badge)
        }
    }

    override fun getItemCount(): Int {
        return summariesList.size
    }
}
