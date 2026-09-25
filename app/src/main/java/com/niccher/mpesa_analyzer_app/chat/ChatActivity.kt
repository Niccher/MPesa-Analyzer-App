package com.niccher.mpesa_analyzer_app.chat

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.niccher.mpesa_analyzer_app.R
import com.niccher.mpesa_analyzer_app.api.ChatApiService
import com.niccher.mpesa_analyzer_app.api.ChatInfoPayload
import com.niccher.mpesa_analyzer_app.api.ChatMessagePayload
import com.niccher.mpesa_analyzer_app.api.ChatRequestPayload
import com.niccher.mpesa_analyzer_app.api.ChatResponsePayload
import com.niccher.mpesa_analyzer_app.helpers.AppPrefs
import com.niccher.mpesa_analyzer_app.helpers.Prefs
import com.niccher.mpesa_analyzer_app.helpers.ServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URI

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: View
    private lateinit var layoutThinking: LinearLayout
    private lateinit var adapter: ChatAdapter
    private val prefs = Prefs()
    private var activeModelName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar: Toolbar = findViewById(R.id.chat_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        rvChat = findViewById(R.id.rv_chat)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)
        layoutThinking = findViewById(R.id.layout_thinking)

        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.layoutManager = layoutManager
        adapter = ChatAdapter()
        rvChat.adapter = adapter

        // Add initial welcoming message from assistant
        adapter.addMessage(
            ChatMessageItem(
                role = "assistant",
                text = "Jambo! 👋 I am your M-Pesa Financial Assistant. You can ask me anything about your monthly spending, Fuliza fees, money sent, or if you can afford an upcoming purchase in English or Sheng!"
            )
        )

        setupChips()
        fetchModelInfo()

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        val initialPrompt = intent.getStringExtra(EXTRA_INITIAL_PROMPT)
        if (!initialPrompt.isNullOrBlank()) {
            sendMessage(initialPrompt)
        }
    }

    private fun setupChips() {
        findViewById<TextView>(R.id.chip_spending_month).setOnClickListener {
            sendMessage("How much did I spend this month compared to last month?")
        }
        findViewById<TextView>(R.id.chip_top_payees).setOnClickListener {
            sendMessage("Who are my top payees by total amount sent?")
        }
        findViewById<TextView>(R.id.chip_fuliza_fees).setOnClickListener {
            sendMessage("How much have I spent on Fuliza and loan fees?")
        }
        findViewById<TextView>(R.id.chip_afford_purchase).setOnClickListener {
            sendMessage("Can I afford a KES 15,000 purchase this weekend based on my bills?")
        }
        findViewById<TextView>(R.id.chip_sheng_tip).setOnClickListener {
            sendMessage("Nipe tips fiti za ku-save chapaa hii mwezi.")
        }
    }


    private fun sendMessage(text: String) {
        val userId = prefs.getPrefsAuth("auth", this).ifBlank { "guest" }

        adapter.addMessage(ChatMessageItem(role = "user", text = text))
        etMessage.text.clear()
        rvChat.scrollToPosition(adapter.itemCount - 1)

        layoutThinking.visibility = View.VISIBLE
        btnSend.isEnabled = false

        val historyPayload = adapter.getHistory().dropLast(1).takeLast(6).map {
            ChatMessagePayload(role = it.role, content = it.text)
        }

        val request = ChatRequestPayload(
            userId = userId,
            message = text,
            history = historyPayload
        )

        val api = ServiceGenerator.createService(ChatApiService::class.java, this)

        api.sendChat(request).enqueue(object : Callback<ChatResponsePayload> {
            override fun onResponse(call: Call<ChatResponsePayload>, response: Response<ChatResponsePayload>) {
                layoutThinking.visibility = View.GONE
                btnSend.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val reply = body.reply
                    if (!body.model.isNullOrBlank()) {
                        activeModelName = body.model
                        supportActionBar?.subtitle = "AI Financial Advisor • ${body.model}"
                    }
                    adapter.addMessage(ChatMessageItem(role = "assistant", text = reply))
                } else {
                    val err = response.errorBody()?.string().orEmpty()
                    adapter.addMessage(
                        ChatMessageItem(
                            role = "assistant",
                            text = "Could not get advice from assistant (HTTP ${response.code()}). Make sure the AI backend service is online."
                        )
                    )
                }
                rvChat.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onFailure(call: Call<ChatResponsePayload>, t: Throwable) {
                layoutThinking.visibility = View.GONE
                btnSend.isEnabled = true
                adapter.addMessage(
                    ChatMessageItem(
                        role = "assistant",
                        text = "Connection error: ${t.localizedMessage ?: "Unable to connect to AI assistant."}"
                    )
                )
                rvChat.scrollToPosition(adapter.itemCount - 1)
            }
        })
    }

    private fun fetchModelInfo() {
        val api = ServiceGenerator.createService(ChatApiService::class.java, this)
        api.getChatInfo().enqueue(object : Callback<ChatInfoPayload> {
            override fun onResponse(call: Call<ChatInfoPayload>, response: Response<ChatInfoPayload>) {
                if (response.isSuccessful && response.body() != null) {
                    val info = response.body()!!
                    if (info.model.isNotBlank()) {
                        activeModelName = info.model
                        supportActionBar?.subtitle = "AI Financial Advisor • ${info.model}"
                    }
                }
            }

            override fun onFailure(call: Call<ChatInfoPayload>, t: Throwable) {
                // Keep default title if service info is unreachable
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_INITIAL_PROMPT = "extra_initial_prompt"
    }
}
