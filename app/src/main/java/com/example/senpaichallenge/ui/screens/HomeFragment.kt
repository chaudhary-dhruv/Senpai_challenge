package com.example.senpaichallenge.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.adapters.QuizAdapter
import com.example.senpaichallenge.adapters.QuizItem
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuizAdapter
    private val quizItems = mutableListOf<QuizItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewQuizzes)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = QuizAdapter(requireContext(), quizItems)
        recyclerView.adapter = adapter

        loadQuizzesFromFirestore()
        return view
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
