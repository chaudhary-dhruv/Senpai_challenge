package com.example.senpaichallenge.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.senpaichallenge.MainActivity
import com.example.senpaichallenge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class DetailActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var animeIdInput: EditText
    private lateinit var btnContinue: Button

    private val avatars = listOf(
        "avatar1", "avatar2", "avatar3", "avatar4", "avatar5",
        "avatar6", "avatar7", "avatar8", "avatar9", "avatar10",
        "avatar11", "avatar12", "avatar13", "avatar14", "avatar15",
        "avatar16", "avatar17", "avatar18", "avatar19", "avatar20",
        "avatar21", "avatar22", "avatar23", "avatar24", "avatar25"
    )

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_detail)

        usernameInput = findViewById(R.id.username_input)
        animeIdInput = findViewById(R.id.anime_id_input)
        btnContinue = findViewById(R.id.btn_register)

        btnContinue.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val animeId = animeIdInput.text.toString().trim()

            if (username.isEmpty() || animeId.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkAnimeIdAndSave(username, animeId)
        }
    }

    private fun checkAnimeIdAndSave(username: String, animeId: String) {
        firestore.collection("users")
            .whereEqualTo("animeId", animeId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Toast.makeText(this, "Anime ID already taken!", Toast.LENGTH_SHORT).show()
                } else {
                    saveUserData(username, animeId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking Anime ID", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData(username: String, animeId: String) {
        val uid = auth.currentUser?.uid
        val email = auth.currentUser?.email

        if (uid == null || email == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val randomAvatar = avatars[Random.nextInt(avatars.size)]

        // Initial score structure
        val animeScores = hashMapOf<String, Int>()
        for (i in 1..15) {
            animeScores["anime_$i"] = 0
        }

        val userMap = hashMapOf(
            "username" to username,
            "animeId" to animeId,
            "avatar" to randomAvatar,
            "score" to 0,
            "animeScores" to animeScores,
            "email" to email
        )

        firestore.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                goToMain()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("openProfile", true)
        startActivity(intent)
        finish()
    }
}
