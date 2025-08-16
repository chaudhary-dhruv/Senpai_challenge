package com.example.senpaichallenge.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.QuizAdapter
import com.example.senpaichallenge.adapters.QuizItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuizAdapter
    private val quizItems = mutableListOf<QuizItem>()

    private lateinit var avatarImage: CircleImageView
    private lateinit var usernameText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 🔹 Header views
        avatarImage = view.findViewById(R.id.imgUserAvatar)
        usernameText = view.findViewById(R.id.tvUsername)

        // 🔹 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewQuizzes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = QuizAdapter(requireContext(), quizItems)
        recyclerView.adapter = adapter

        // 🔹 Firestore se user data aur quiz list load karo
        loadUserData()
        loadQuizzesFromFirestore()
        return view
    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val username = doc.getString("username") ?: "Guest"
                    val avatarName = doc.getString("avatar") ?: "avatar1"

                    // 🔹 string -> drawable
                    val resId = resources.getIdentifier(
                        avatarName,
                        "drawable",
                        requireContext().packageName
                    )
                    if (resId != 0) {
                        avatarImage.setImageResource(resId)
                    } else {
                        avatarImage.setImageResource(R.drawable.avatar1)
                    }

                    usernameText.text = "Welcome $username"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadQuizzesFromFirestore() {
        FirebaseFirestore.getInstance().collection("quizzes")
            .get()
            .addOnSuccessListener { documents ->
                quizItems.clear()
                for (doc in documents) {
                    val animeName = doc.id
                    quizItems.add(QuizItem(animeName))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load quizzes", Toast.LENGTH_SHORT).show()
            }
    }
}
