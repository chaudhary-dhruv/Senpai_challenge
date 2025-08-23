package com.example.senpaichallenge.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.ChatBubbleAdapter
import com.example.senpaichallenge.models.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvUserName: TextView
    private lateinit var imgAvatar: CircleImageView
    private lateinit var inputBar: View

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
        inputBar = findViewById(R.id.inputBar)

        // Intent data
        receiverId = intent.getStringExtra("receiverId") ?: ""
        username = intent.getStringExtra("username") ?: ""
        avatar = intent.getStringExtra("avatar") ?: ""

        tvUserName.text = username
        val resId = resources.getIdentifier(avatar, "drawable", packageName)
        if (resId != 0) imgAvatar.setImageResource(resId)

        val lm = LinearLayoutManager(this).apply {
            stackFromEnd = true // last message visible at bottom
        }
        recyclerView.layoutManager = lm
        adapter = ChatBubbleAdapter(messages)
        recyclerView.adapter = adapter

        // Keep inputBar above keyboard & nav bar
        ViewCompat.setOnApplyWindowInsetsListener(inputBar) { v, insets ->
            val ime = insets.getInsets(
                WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(ime.left, v.paddingTop, ime.right, ime.bottom)
            insets
        }

        // Add extra bottom padding to list so last msg not hidden by inputBar/IME
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val ime = insets.getInsets(
                WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, ime.bottom + v.paddingBottom)
            insets
        }

        if (receiverId.isNotEmpty()) {
            loadMessages()
        }

        btnSend.setOnClickListener { sendMessage() }
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }
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
        if (text.isEmpty() || receiverId.isEmpty() || currentUserId.isEmpty()) return

        val chatId = getChatId(currentUserId, receiverId)
        val chatRef = db.collection("chats").document(chatId)

        val msg = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        // ensure chat meta exists (participants etc.)
        val chatMeta = mapOf(
            "participants" to listOf(currentUserId, receiverId),
            "lastMessage" to text,
            "lastTimestamp" to msg.timestamp
        )
        chatRef.set(chatMeta, SetOptions.merge())

        chatRef.collection("messages")
            .add(msg)
            .addOnSuccessListener {
                edtMessage.setText("")
                recyclerView.scrollToPosition(messages.size - 1)
            }
            .addOnFailureListener { it.printStackTrace() }
    }

    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
