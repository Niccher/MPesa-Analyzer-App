package com.niccher.my_mpesa_analyzer.adapter

import com.niccher.my_mpesa_analyzer.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.niccher.my_mpesa_analyzer.fragments.Info.Info_Data

class Adapter_Frag_Info(private val items: List<Info_Data>) : RecyclerView.Adapter<Adapter_Frag_Info.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTitleView: TextView = itemView.findViewById(R.id.acc_item)
        val idDescription: TextView = itemView.findViewById(R.id.acc_item_description)
        val idIcon: ImageView = itemView.findViewById(R.id.acc_item_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.part_info_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = items[position]
        holder.idTitleView.text = currentItem.name_title.toString()
        holder.idDescription.text = currentItem.name_desc

        holder.idIcon.setImageResource(currentItem.name_icon) // Replace with your data binding
    }

    override fun getItemCount() = items.size
}