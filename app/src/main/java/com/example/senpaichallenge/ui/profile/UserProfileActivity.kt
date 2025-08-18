package com.example.senpaichallenge.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.senpaichallenge.R
import de.hdodenhof.circleimageview.CircleImageView

class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val imgUserAvatar: CircleImageView = findViewById(R.id.imgUserAvatar)
        val tvUsername: TextView = findViewById(R.id.tvUsername)
        val tvAnimeId: TextView = findViewById(R.id.tvAnimeId)
        val tvBio: TextView = findViewById(R.id.tvBio)
        val tvRank: TextView = findViewById(R.id.tvRank)
        val tvPoints: TextView = findViewById(R.id.tvPoints)
        val btnMessage: Button = findViewById(R.id.btnMessage)

        // 🔹 Back button
        btnBack.setOnClickListener { finish() }

        // 🔹 Get data from Intent
        val username = intent.getStringExtra("username")
        val animeId = intent.getStringExtra("animeId")
        val avatar = intent.getStringExtra("avatar")
        val bio = intent.getStringExtra("bio")
        val rank = intent.getIntExtra("rank", -1)
        val points = intent.getIntExtra("points", 0)

        // 🔹 Set data
        tvUsername.text = username
        tvAnimeId.text = animeId
        tvBio.text = bio ?: "No bio available"
        tvRank.text = if (rank > 0) rank.toString() else "-"
        tvPoints.text = "$points pts"

        val resId = resources.getIdentifier(avatar, "drawable", packageName)
        if (resId != 0) {
            imgUserAvatar.setImageResource(resId)
        }

        btnMessage.setOnClickListener {
            // TODO: Message screen open (future step)
        }
    }
}
