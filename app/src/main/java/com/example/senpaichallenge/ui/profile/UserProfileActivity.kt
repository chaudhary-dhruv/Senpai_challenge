package com.example.senpaichallenge.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.senpaichallenge.R
import com.example.senpaichallenge.ui.screens.ChatActivity
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class UserProfileActivity : AppCompatActivity() {

    private lateinit var imgUserAvatar: CircleImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvAnimeId: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvRank: TextView
    private lateinit var tvPoints: TextView
    private lateinit var btnMessage: Button

    private val db = FirebaseFirestore.getInstance()
    private var uid: String? = null
    private var avatar: String? = null
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        imgUserAvatar = findViewById(R.id.imgUserAvatar)
        tvUsername = findViewById(R.id.tvUsername)
        tvAnimeId = findViewById(R.id.tvAnimeId)
        tvBio = findViewById(R.id.tvBio)
        tvRank = findViewById(R.id.tvRank)
        tvPoints = findViewById(R.id.tvPoints)
        btnMessage = findViewById(R.id.btnMessage)

        // 🔹 Back button
        val btnback: ImageButton = findViewById(R.id.btnBack)
        btnback.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()  // ✅ recommended
        }


        // 🔹 Intent data
        uid = intent.getStringExtra("uid")
        username = intent.getStringExtra("username")
        avatar = intent.getStringExtra("avatar")

        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🔹 Firestore se full user data load karo
        loadUserProfile(uid!!)

        // 🔹 Message button
        btnMessage.setOnClickListener {
            uid?.let { targetId ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("receiverId", targetId)
                    putExtra("username", username)
                    putExtra("avatar", avatar)
                }
                startActivity(intent)
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    username = doc.getString("username") ?: "Unknown"
                    val animeId = doc.getString("animeId") ?: "-"
                    avatar = doc.getString("avatar") ?: "avatar1"
                    val bio = doc.getString("bio") ?: "No bio yet! ✨"

                    // ✅ Rank calculate dynamically from all users
                    val points = doc.getLong("totalPoints")?.toInt() ?: 0

                    // Pehle points set karte hain
                    tvPoints.text = "$points pts"

                    // 🔹 Rank nikalne ke liye query sab users ka order by totalPoints
                    db.collection("users")
                        .orderBy("totalPoints", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            var rank = "-"
                            snapshot.documents.forEachIndexed { index, userDoc ->
                                if (userDoc.id == userId) {
                                    rank = (index + 1).toString()
                                }
                            }
                            tvRank.text = rank
                        }
                        .addOnFailureListener {
                            tvRank.text = "-"
                        }

                    // 🔹 Set views
                    tvUsername.text = username
                    tvAnimeId.text = animeId
                    tvBio.text = bio

                    // Avatar set
                    val resId = resources.getIdentifier(avatar, "drawable", packageName)
                    if (resId != 0) {
                        imgUserAvatar.setImageResource(resId)
                    } else {
                        imgUserAvatar.setImageResource(R.drawable.avatar1)
                    }
                } else {
                    Toast.makeText(this, "Profile not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }
}
