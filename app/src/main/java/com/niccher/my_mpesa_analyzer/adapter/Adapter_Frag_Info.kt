package com.niccher.my_mpesa_analyzer.adapter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
import com.niccher.my_mpesa_analyzer.R
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

    private val profileBottomSheet: BottomSheetDialog = BottomSheetDialog(context).apply {
        setContentView(R.layout.part_sheet_profile)
    }

    private val appInfoBottomSheet: BottomSheetDialog = BottomSheetDialog(context).apply {
        setContentView(R.layout.part_sheet_app_info)
    }

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

    private fun getAppVersion(): String {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = items[position]
        holder.idTitleView.text = currentItem.name_title.toString()
        holder.idDescription.text = currentItem.name_desc
        holder.idIcon.setImageResource(currentItem.name_icon)

        holder.partBody.setOnClickListener {
            when (currentItem.name_title) {
                "Profile" -> showProfileBottomSheet()
                "App Info" -> showAppInfoBottomSheet()
                "App Credits" -> showAppCredits()
                else -> Toast.makeText(context, "${currentItem.name_title} clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProfileBottomSheet() {
        // Set profile data
        profileBottomSheet.findViewById<TextView>(R.id.user_profile_name)?.text = getPrefsUserData("userName")
        profileBottomSheet.findViewById<TextView>(R.id.user_profile_email)?.text = getPrefsUserData("userEmail")
        profileBottomSheet.findViewById<TextView>(R.id.user_profile_time)?.text = "Logged in at ${getPrefsUserData("time")}"

        // Set click listeners
        profileBottomSheet.findViewById<CardView>(R.id.card_logout)?.setOnClickListener {
            val prefLogging = context.getSharedPreferences(kon.SHARED_AUTH_LOGIN, Context.MODE_PRIVATE)
            prefLogging.edit().clear().apply()

            val logoutIntent = Intent(context, Sign_In::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(logoutIntent)
            profileBottomSheet.dismiss()
        }

        profileBottomSheet.findViewById<CardView>(R.id.card_del)?.setOnClickListener {
            Toast.makeText(context, "Account deletion feature", Toast.LENGTH_SHORT).show()
            profileBottomSheet.dismiss()
        }

        profileBottomSheet.findViewById<CardView>(R.id.card_back)?.setOnClickListener {
            profileBottomSheet.dismiss()
        }

        profileBottomSheet.show()
    }

    private fun showAppInfoBottomSheet() {
        // Set app info data
        appInfoBottomSheet.findViewById<TextView>(R.id.app_info_version)?.text = "Version ${getAppVersion()}"
        appInfoBottomSheet.findViewById<TextView>(R.id.app_info_description)?.text = "M-Pesa SMS Parser & Transaction Analyzer"

        // Set click listeners for app info cards
        appInfoBottomSheet.findViewById<CardView>(R.id.card_features)?.setOnClickListener {
            Toast.makeText(context, "App Features", Toast.LENGTH_SHORT).show()
        }

        appInfoBottomSheet.findViewById<CardView>(R.id.card_permissions)?.setOnClickListener {
            Toast.makeText(context, "App Permissions", Toast.LENGTH_SHORT).show()
        }

        appInfoBottomSheet.findViewById<CardView>(R.id.card_developer)?.setOnClickListener {
            Toast.makeText(context, "Developer Info", Toast.LENGTH_SHORT).show()
        }

        appInfoBottomSheet.findViewById<CardView>(R.id.card_app_info_back)?.setOnClickListener {
            appInfoBottomSheet.dismiss()
        }

        appInfoBottomSheet.show()
    }

    private fun showAppCredits() {
        Toast.makeText(context, "App Credits - Coming Soon", Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount() = items.size
}