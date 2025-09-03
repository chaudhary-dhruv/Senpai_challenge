package com.example.senpaichallenge.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.senpaichallenge.MainActivity
import com.example.senpaichallenge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.random.Random

class DetailActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var animeIdInput: EditText
    private lateinit var btnContinue: Button

    // 25 predefined avatars
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

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

    //Step 1: Check if AnimeID already exists
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

    //Step 2: Save new user data
    private fun saveUserData(username: String, animeId: String) {
        val uid = auth.currentUser?.uid
        val email = auth.currentUser?.email

        if (uid == null || email == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val randomAvatar = avatars[Random.nextInt(avatars.size)]

        // All anime list (for quiz progress tracking)
        val animeList = listOf(
            "naruto", "one_punch_man", "my_hero_academia", "akira",
            "your_name", "hunter_x_hunter", "bleach", "one_piece",
            "demon_slayer", "attack_on_titan", "dragon_ball_z",
            "jujutsu_kaisen", "spirited_away", "boruto", "death_note"
        )

        // Initialize anime points & progress index
        val animePointsMap = hashMapOf<String, Int>()
        val lastIndexMap = hashMapOf<String, Int>()
        animeList.forEach { anime ->
            animePointsMap[anime] = 0
            lastIndexMap[anime] = 0
        }

        // Final user object for Firestore
        val userMap = hashMapOf(
            "uid" to uid,                       // Correct field for UID
            "username" to username,             // Correct field for username
            "animeId" to animeId,               // Correct field for anime ID
            "avatar" to randomAvatar,           // Correct field for avatar
            "email" to email,                   // Correct field for email
            "totalPoints" to 0,                 // Correct field for points
            "animePoints" to animePointsMap,    // nested map for per-anime points
            "lastIndex" to lastIndexMap,        // nested map for quiz progress
            "bio" to "No bio yet! ✨"           // Default bio
        )

        firestore.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                //FCM token save after profile created
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    firestore.collection("users").document(uid)
                        .update("fcmToken", token)
                }

                Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                goToMain()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
    }

    //Step 3: Go to Main screen
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("openProfile", true)
        startActivity(intent)
        finish()
    }
}
