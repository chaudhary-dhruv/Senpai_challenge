package com.example.senpaichallenge.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.ChatListAdapter
import com.example.senpaichallenge.models.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: ChatListAdapter
    private val users = mutableListOf<ChatUser>()

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerView = view.findViewById(R.id.rvMessages)
        etSearch = view.findViewById(R.id.etSearch)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatListAdapter(users) { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("receiverId", user.userId)   // correct UID
            intent.putExtra("username", user.username)
            intent.putExtra("avatar", user.avatar)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // 🔹 Load chats instead of just users
        loadChats()

        return view
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

                    // find the other user (receiver)
                    val otherUserId = participants.firstOrNull { it != currentUserId } as? String ?: continue

                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastSeen = doc.getLong("lastTimestamp") ?: 0L

                    // 🔹 fetch user info from users/{uid}
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
                                lastSeen = lastSeen
                            )

                            // avoid duplicates
                            val index = users.indexOfFirst { it.userId == otherUserId }
                            if (index >= 0) {
                                users[index] = chatUser
                            } else {
                                users.add(chatUser)
                            }
                            adapter.notifyDataSetChanged()
                        }
                }
            }
    }
}
