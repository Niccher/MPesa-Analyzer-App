package com.niccher.my_mpesa_analyzer.adapter

import android.content.Context
import android.content.Intent
import com.niccher.my_mpesa_analyzer.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.niccher.my_mpesa_analyzer.auth.Sign_In
import com.niccher.my_mpesa_analyzer.fragments.Info.Info_Data
import com.niccher.my_mpesa_analyzer.konstants.Konstants

class Adapter_Frag_Info(private val context: Context, private val items: List<Info_Data>) : RecyclerView.Adapter<Adapter_Frag_Info.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTitleView: TextView = itemView.findViewById(R.id.acc_item)
        val idDescription: TextView = itemView.findViewById(R.id.acc_item_description)
        val idIcon: ImageView = itemView.findViewById(R.id.acc_item_img)
        val partBody: ConstraintLayout = itemView.findViewById(R.id.acc_body)
    }

    private val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context).apply {
        setContentView(R.layout.part_sheet_profile)
    }

    private val cardLogOut: CardView? = bottomSheetDialog.findViewById(R.id.card_logout)
    private val cardDeleteAccount: CardView? = bottomSheetDialog.findViewById(R.id.card_del)
    private val cardBack: CardView? = bottomSheetDialog.findViewById(R.id.card_back)
    private val myName: TextView? = bottomSheetDialog.findViewById(R.id.user_profile_name)
    private val myEmail: TextView? = bottomSheetDialog.findViewById(R.id.user_profile_email)
    private val myLoggedTime: TextView? = bottomSheetDialog.findViewById(R.id.user_profile_time)

    private val kon: Konstants = Konstants

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.part_info_item, parent, false)
        return MyViewHolder(itemView)
    }

    private fun getPrefsUserData(field: String): String {
        val prefs = context.getSharedPreferences(kon.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
        val data = prefs.getString(field, "nullable") ?: "nullable"
        return data.replaceFirstChar { it.uppercase() }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = items[position]
        holder.idTitleView.text = currentItem.name_title.toString()
        holder.idDescription.text = currentItem.name_desc
        holder.idIcon.setImageResource(currentItem.name_icon)

        myName?.text = getPrefsUserData("userName")
        myEmail?.text = getPrefsUserData("userEmail")
        myLoggedTime?.text = "Logged in at ${getPrefsUserData("time")}"

        holder.partBody.setOnClickListener {
            if (currentItem.name_title == "Profile") {
                bottomSheetDialog.show()

                cardLogOut?.setOnClickListener {
                    val prefLogging = context.getSharedPreferences(kon.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
                    prefLogging.edit().clear().apply()

                    val logoutIntent = Intent(context, Sign_In::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(logoutIntent)
                }

                cardDeleteAccount?.setOnClickListener {
                    Toast.makeText(context, "Sign profile_delete", Toast.LENGTH_SHORT).show()
                }

                cardBack?.setOnClickListener {
                    bottomSheetDialog.dismiss()
                }
            }
        }

    }

    override fun getItemCount() = items.size
}