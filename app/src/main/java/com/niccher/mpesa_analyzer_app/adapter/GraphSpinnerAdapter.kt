package com.niccher.mpesa_analyzer_app.adapter

import android.content.Context
import com.niccher.mpesa_analyzer_app.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.niccher.mpesa_analyzer_app.fragments.Info.Info_Data

class GraphSpinnerAdapter(
    private val context: Context,
    private val smsCatImg: IntArray,
    private val smsCatName: Array<String>
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = smsCatName.size

    override fun getItem(position: Int): Any? = null

    override fun getItemId(position: Int): Long = 0L

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.part_sms_category_spinner, parent, false)

        val partSmsCat = view.findViewById<TextView>(R.id.part_spinner_txt)
        val partSmsImg = view.findViewById<ImageView>(R.id.part_spinner_img)

        partSmsImg.setImageResource(smsCatImg[position])
        partSmsCat.text = smsCatName[position]

        return view
    }
}
