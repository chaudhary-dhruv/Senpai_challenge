package com.example.senpaichallenge.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.senpaichallenge.R
import com.example.senpaichallenge.ui.profile.EditProfileActivity
import com.example.senpaichallenge.ui.profile.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var avatarImage: CircleImageView
    private lateinit var usernameText: TextView
    private lateinit var bioText: TextView
    private lateinit var tvRank: TextView
    private lateinit var tvPoints: TextView
    private lateinit var tvCurrentPoints: TextView
    private lateinit var tvTotalPoints: TextView
    private lateinit var pointsProgressBar: ProgressBar
    private lateinit var spinner: Spinner

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Total max points
    private val TOTAL_MAX_POINTS = 750000
    private val ANIME_MAX_POINTS = 50000



    // Anime list
    private val animeList = listOf(
        "All", "naruto", "one_punch_man", "my_hero_academia", "akira",
        "your_name", "hunter_x_hunter", "bleach", "one_piece",
        "demon_slayer", "attack_on_titan", "dragon_ball_z",
        "jujutsu_kaisen", "spirited_away", "boruto", "death_note"
    )

    private var userTotalPoints = 0
    private var userAnimePoints: Map<String, Int> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Views init
        avatarImage = view.findViewById(R.id.imgUserAvatar)
        usernameText = view.findViewById(R.id.tvUsername)
        bioText = view.findViewById(R.id.tvBio)
        tvRank = view.findViewById(R.id.tvRank)
        tvPoints = view.findViewById(R.id.tvPoints)
        tvCurrentPoints = view.findViewById(R.id.tvCurrentPoints)
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints)
        pointsProgressBar = view.findViewById(R.id.pointsProgressBar)
        spinner = view.findViewById(R.id.pointsFilterSpinner)

        //Edit Profile
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        //Setting icon
        view.findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }



        // Spinner setup
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, animeList)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        loadUserData()
        loadRanking()

        // Spinner selection change listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                updatePointsUI(animeList[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val username = doc.getString("username") ?: "Guest"
                    val animeId = doc.getString("animeId") ?: "UnknownID"
                    val avatarName = doc.getString("avatar") ?: "avatar1"
                    val bio = doc.getString("bio") ?: "No bio yet! ✨"

                    userTotalPoints = (doc.getLong("totalPoints") ?: 0L).toInt()

                    // animePoints fetch
                    userAnimePoints = (doc.get("animePoints") as? Map<String, Long>)
                        ?.mapValues { it.value.toInt() } ?: emptyMap()

                    // Avatar load
                    val resId = resources.getIdentifier(avatarName, "drawable", requireContext().packageName)
                    if (resId != 0) avatarImage.setImageResource(resId)
                    else avatarImage.setImageResource(R.drawable.avatar1)

                    //  Data bind (swapped as per your request)
                    usernameText.text = animeId   // tvUsername → animeId
                    view?.findViewById<TextView>(R.id.tvBadge)?.text = username  // tvBadge → username

                    bioText.text = bio
                    tvPoints.text = userTotalPoints.toString()

                    // Default spinner = All
                    updatePointsUI("All")
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadRanking() {
        firestore.collection("users")
            .orderBy("totalPoints", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { docs ->
                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                var rank = 1
                for (doc in docs) {
                    if (doc.id == uid) {
                        tvRank.text = rank.toString()
                        break
                    }
                    rank++
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load ranking", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePointsUI(selectedAnime: String) {
        if (selectedAnime == "All") {
            // All anime total
            tvCurrentPoints.text = userTotalPoints.toString()
            tvTotalPoints.text = TOTAL_MAX_POINTS.toString()
            pointsProgressBar.max = TOTAL_MAX_POINTS
            pointsProgressBar.progress = userTotalPoints
        } else {
            // Specific anime
            val animePoints = userAnimePoints[selectedAnime] ?: 0
            tvCurrentPoints.text = animePoints.toString()
            tvTotalPoints.text = ANIME_MAX_POINTS.toString()
            pointsProgressBar.max = ANIME_MAX_POINTS
            pointsProgressBar.progress = animePoints
        }
    }
}
