package com.example.senpaichallenge.ui.screens

import android.content.Intent
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
import com.example.senpaichallenge.ui.profile.UserProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
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
    private var bio: String = ""
    private var rank: Int = -1
    private var points: Int = 0

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

        //  Fetch receiver profile & messages
        if (receiverId.isNotEmpty()) {
            loadReceiverProfile(receiverId)
            loadMessages()
        }

        val lm = LinearLayoutManager(this).apply { stackFromEnd = true }
        recyclerView.layoutManager = lm
        adapter = ChatBubbleAdapter(messages)
        recyclerView.adapter = adapter

        // Insets handle
        ViewCompat.setOnApplyWindowInsetsListener(inputBar) { v, insets ->
            val ime = insets.getInsets(
                WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(ime.left, v.paddingTop, ime.right, ime.bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val ime = insets.getInsets(
                WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, ime.bottom)
            insets
        }

        btnSend.setOnClickListener { sendMessage() }
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }

        //  Username & Avatar click → Open UserProfileActivity
        val openProfile = View.OnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java).apply {
                putExtra("uid", receiverId)
                putExtra("username", username)
                putExtra("avatar", avatar)
                putExtra("bio", bio)
                putExtra("rank", rank)
                putExtra("points", points)
            }
            startActivity(intent)
        }
        tvUserName.setOnClickListener(openProfile)
        imgAvatar.setOnClickListener(openProfile)
    }

    // Receiver profile load from Firestore
    private fun loadReceiverProfile(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    username = doc.getString("username") ?: "Unknown"
                    avatar = doc.getString("avatar") ?: "avatar1"
                    bio = doc.getString("bio") ?: "No bio available"
                    rank = doc.getLong("rank")?.toInt() ?: -1
                    points = doc.getLong("points")?.toInt() ?: 0

                    // UI update
                    tvUserName.text = username
                    val resId = resources.getIdentifier(avatar, "drawable", packageName)
                    if (resId != 0) imgAvatar.setImageResource(resId)
                }
            }
    }

    private fun loadMessages() {
        val chatId = getChatId(currentUserId, receiverId)
        val chatRef = db.collection("chats").document(chatId)

        chatRef.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshots) {
                    val msg = doc.toObject(ChatMessage::class.java)
                    messages.add(msg)

                    //  If I am receiver → mark as seen
                    if (msg.receiverId == currentUserId && !msg.seen) {
                        doc.reference.update("seen", true)
                    }
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)

                // Reset unreadCount for me
                chatRef.update("unreadCount.$currentUserId", 0)
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
            timestamp = System.currentTimeMillis(),
            seen = false
        )

        val chatMeta = mapOf(
            "participants" to listOf(currentUserId, receiverId),
            "lastMessage" to text,
            "lastTimestamp" to msg.timestamp
        )
        chatRef.set(chatMeta, SetOptions.merge())

        //  unreadCount increase only for receiver
        chatRef.update("unreadCount.$receiverId", FieldValue.increment(1))

        chatRef.collection("messages")
            .add(msg)
            .addOnSuccessListener {
                edtMessage.setText("")
                recyclerView.scrollToPosition(messages.size - 1)
            }
    }

    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
