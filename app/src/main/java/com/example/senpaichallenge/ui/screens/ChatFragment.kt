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
            // 🔹 Jab user par click ho → ChatActivity open
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("receiverId", user.userId)
            intent.putExtra("username", user.username)
            intent.putExtra("avatar", user.avatar)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadUsers()

        return view
    }

    private fun loadUsers() {
        db.collection("users")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                users.clear()
                for (doc in snapshots) {
                    val user = doc.toObject(ChatUser::class.java)
                    if (user.userId != currentUserId) {
                        users.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
