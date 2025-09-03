package com.example.senpaichallenge.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.LeaderboardAdapter
import com.example.senpaichallenge.models.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private val userList = mutableListOf<UserModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        recyclerView = view.findViewById(R.id.recyclerLeaderboard)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LeaderboardAdapter(userList)
        recyclerView.adapter = adapter

        loadLeaderboard()

        return view
    }

    private fun loadLeaderboard() {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { docs ->
                userList.clear()
                for ((index, doc) in docs.withIndex()) {
                    val uid = doc.id
                    val username = doc.getString("username") ?: "Unknown"
                    val animeId = doc.getString("animeId") ?: "UnknownID"
                    val avatar = doc.getString("avatar") ?: "avatar1"
                    val points = (doc.getLong("totalPoints") ?: 0L).toInt()
                    val bio = doc.getString("bio") ?: ""

                    userList.add(
                        UserModel(
                            uid = uid,
                            username = username,
                            animeId = animeId,
                            avatar = avatar,
                            points = points,
                            bio = bio
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
    }
}
