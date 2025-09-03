package com.example.senpaichallenge.ui.screens

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.ChatListAdapter
import com.example.senpaichallenge.models.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: ChatListAdapter

    private val users = mutableListOf<ChatUser>()

    private lateinit var avatarImage: CircleImageView
    private lateinit var usernameText: TextView

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        avatarImage = view.findViewById(R.id.imgAvatar)
        usernameText = view.findViewById(R.id.tvUserName)

        recyclerView = view.findViewById(R.id.rvMessages)
        etSearch = view.findViewById(R.id.etSearch)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatListAdapter(users) { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("receiverId", user.userId)
            intent.putExtra("username", user.username)
            intent.putExtra("avatar", user.avatar)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadCurrentUser()
        loadChats() // by default chat list loaded

        // Firestore search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    loadChats() // If search box is clear than show the chat list
                } else {
                    searchUsers(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun loadCurrentUser() {
        if (currentUserId.isEmpty()) return
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val username = doc.getString("username") ?: "Guest"
                    val avatarName = doc.getString("avatar") ?: "avatar1"

                    val resId = resources.getIdentifier(
                        avatarName,
                        "drawable",
                        requireContext().packageName
                    )
                    if (resId != 0) avatarImage.setImageResource(resId)
                    usernameText.text = username
                }
            }
    }

    private fun loadChats() {
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                users.clear()
                for (doc in snapshots) {
                    val participants = doc.get("participants") as? List<*>
                    if (participants.isNullOrEmpty()) continue

                    val otherUserId = participants.firstOrNull { it != currentUserId } as? String ?: continue
                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastSeen = doc.getLong("lastTimestamp") ?: 0L

                    val unreadMap = doc.get("unreadCount") as? Map<*, *>
                    val unreadCount = (unreadMap?.get(currentUserId) as? Long)?.toInt() ?: 0

                    db.collection("users").document(otherUserId).get()
                        .addOnSuccessListener { userDoc ->
                            val username = userDoc.getString("username") ?: ""
                            val animeId = userDoc.getString("animeId") ?: ""
                            val avatar = userDoc.getString("avatar") ?: "avatar1"

                            val chatUser = ChatUser(
                                userId = otherUserId,
                                username = if (username.isNotEmpty()) username else animeId,
                                animeId = animeId,
                                avatar = avatar,
                                lastMessage = lastMessage,
                                lastSeen = lastSeen,
                                unreadCount = unreadCount
                            )

                            if (users.none { it.userId == otherUserId }) {
                                users.add(chatUser)
                            }
                            adapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun searchUsers(query: String) {
        users.clear()
        val lowerQuery = query.lowercase()

        //  Username search
        db.collection("users")
            .orderBy("username")
            .startAt(lowerQuery)
            .endAt(lowerQuery + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val userId = doc.id
                    if (userId == currentUserId) continue // apna profile skip
                    val username = doc.getString("username") ?: ""
                    val animeId = doc.getString("animeId") ?: ""
                    val avatar = doc.getString("avatar") ?: "avatar1"

                    val chatUser = ChatUser(
                        userId = userId,
                        username = username,
                        animeId = animeId,
                        avatar = avatar,
                        lastMessage = "",
                        lastSeen = 0,
                        unreadCount = 0
                    )

                    if (users.none { it.userId == userId }) {
                        users.add(chatUser)
                    }
                }

                // AnimeID search bhi run karo
                db.collection("users")
                    .orderBy("animeId")
                    .startAt(lowerQuery)
                    .endAt(lowerQuery + "\uf8ff")
                    .get()
                    .addOnSuccessListener { animeResult ->
                        for (doc in animeResult) {
                            val userId = doc.id
                            if (userId == currentUserId) continue
                            val username = doc.getString("username") ?: ""
                            val animeId = doc.getString("animeId") ?: ""
                            val avatar = doc.getString("avatar") ?: "avatar1"

                            val chatUser = ChatUser(
                                userId = userId,
                                username = username,
                                animeId = animeId,
                                avatar = avatar,
                                lastMessage = "",
                                lastSeen = 0,
                                unreadCount = 0
                            )

                            if (users.none { it.userId == userId }) {
                                users.add(chatUser)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
            }
    }
}
