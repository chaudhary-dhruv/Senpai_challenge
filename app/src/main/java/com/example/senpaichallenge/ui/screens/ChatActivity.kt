package com.example.senpaichallenge.ui.screens

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.ChatBubbleAdapter
import com.example.senpaichallenge.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvUserName: TextView
    private lateinit var imgAvatar: CircleImageView

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatBubbleAdapter

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var receiverId: String = ""
    private var username: String = ""
    private var avatar: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerChat)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)
        tvUserName = findViewById(R.id.tvUserName)
        imgAvatar = findViewById(R.id.imgAvatar)

        // 🔹 Intent se data get
        receiverId = intent.getStringExtra("receiverId") ?: ""
        username = intent.getStringExtra("username") ?: ""
        avatar = intent.getStringExtra("avatar") ?: ""

        tvUserName.text = username
        val resId = resources.getIdentifier(avatar, "drawable", packageName)
        if (resId != 0) imgAvatar.setImageResource(resId)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatBubbleAdapter(messages)
        recyclerView.adapter = adapter

        if (receiverId.isNotEmpty()) {
            loadMessages()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadMessages() {
        val chatId = getChatId(currentUserId, receiverId)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshots) {
                    val msg = doc.toObject(ChatMessage::class.java)
                    messages.add(msg)
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage() {
        val text = edtMessage.text.toString().trim()
        if (text.isEmpty() || receiverId.isEmpty()) return

        val chatId = getChatId(currentUserId, receiverId)
        val msg = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(msg)

        edtMessage.setText("")
    }

    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
