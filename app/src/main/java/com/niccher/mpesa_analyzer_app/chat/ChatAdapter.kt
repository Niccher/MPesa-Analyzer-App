package com.niccher.mpesa_analyzer_app.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.niccher.mpesa_analyzer_app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessageItem(
    val role: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter(
    private val messages: MutableList<ChatMessageItem> = mutableListOf()
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun addMessage(msg: ChatMessageItem) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    fun getHistory(): List<ChatMessageItem> = messages.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        val timeStr = timeFormat.format(Date(msg.timestamp))

        if (msg.role == "user") {
            holder.layoutUser.visibility = View.VISIBLE
            holder.layoutAssistant.visibility = View.GONE
            holder.tvUserText.text = msg.text
            holder.tvUserTime.text = timeStr
        } else {
            holder.layoutUser.visibility = View.GONE
            holder.layoutAssistant.visibility = View.VISIBLE
            holder.tvAssistantText.text = msg.text
            holder.tvAssistantTime.text = timeStr
        }
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutUser: View = itemView.findViewById(R.id.layout_user_message)
        val tvUserText: TextView = itemView.findViewById(R.id.tv_user_text)
        val tvUserTime: TextView = itemView.findViewById(R.id.tv_user_time)

        val layoutAssistant: View = itemView.findViewById(R.id.layout_assistant_message)
        val tvAssistantText: TextView = itemView.findViewById(R.id.tv_assistant_text)
        val tvAssistantTime: TextView = itemView.findViewById(R.id.tv_assistant_time)
    }
}
