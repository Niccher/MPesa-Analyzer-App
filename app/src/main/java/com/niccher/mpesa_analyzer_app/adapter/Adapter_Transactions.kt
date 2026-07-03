package com.niccher.mpesa_analyzer_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.models.Mod_Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Adapter_Transactions(private var transList: ArrayList<Mod_Transaction>) :
    RecyclerView.Adapter<Adapter_Transactions.TransViewHolder>(), Filterable {

    private var transListFull: ArrayList<Mod_Transaction> = ArrayList(transList)
    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))

    class TransViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtType: TextView = itemView.findViewById(R.id.txt_trans_type)
        val txtDate: TextView = itemView.findViewById(R.id.txt_trans_date)
        val txtName: TextView = itemView.findViewById(R.id.txt_trans_name)
        val txtAmount: TextView = itemView.findViewById(R.id.txt_trans_amount)
        val txtRef: TextView = itemView.findViewById(R.id.txt_trans_ref)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.part_transaction, parent, false)
        return TransViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransViewHolder, position: Int) {
        val trans = transList[position]

        holder.txtType.text = trans.type
        holder.txtDate.text = dateFormat.format(Date(trans.date))
        holder.txtName.text = trans.name.ifEmpty { trans.address }
        holder.txtRef.text = "REF: ${trans.reference}"

        val amountStr = currencyFormat.format(trans.amount).replace("KES", "Ksh")
        holder.txtAmount.text = amountStr

        // Color coding based on type
        val colorRes = when (trans.type.lowercase()) {
            "received" -> R.color.bg_green
            "sent", "paybill", "withdraw" -> R.color.bg_red
            else -> R.color.colorPrimaryDark
        }
        holder.txtAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        val badgeColorRes = when (trans.type.lowercase()) {
            "received" -> R.color.bg_green
            "sent" -> R.color.bg_red
            "paybill" -> R.color.purple_700
            "withdraw" -> R.color.teal_700
            else -> R.color.colorPrimaryDark
        }
        holder.txtType.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(holder.itemView.context, badgeColorRes)
        )
    }

    override fun getItemCount(): Int = transList.size

    override fun getFilter(): Filter {
        return transactionFilter
    }

    private val transactionFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<Mod_Transaction>()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(transListFull)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                for (item in transListFull) {
                    if (item.name.lowercase().contains(filterPattern) ||
                        item.body.lowercase().contains(filterPattern) ||
                        item.reference.lowercase().contains(filterPattern) ||
                        item.type.lowercase().contains(filterPattern)
                    ) {
                        filteredList.add(item)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            transList.clear()
            transList.addAll(results?.values as ArrayList<Mod_Transaction>)
            notifyDataSetChanged()
        }
    }

    fun updateList(newList: List<Mod_Transaction>) {
        transListFull = ArrayList(newList)
        transList.clear()
        transList.addAll(newList)
        notifyDataSetChanged()
    }
}
