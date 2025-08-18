package com.example.senpaichallenge.ui.profile



import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.senpaichallenge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etBio: EditText
    private lateinit var btnApplyChanges: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etUsername = findViewById(R.id.etUsername)
        etBio = findViewById(R.id.etBio)
        btnApplyChanges = findViewById(R.id.btnApplyChanges)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        // Back button
        btnBack.setOnClickListener { finish() }

        // Load existing data
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etUsername.setText(doc.getString("username") ?: "")
                    etBio.setText(doc.getString("bio") ?: "")
                }
            }

        // Apply Changes
        btnApplyChanges.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (username.length > 25 || bio.length > 100) {
                Toast.makeText(this, "Username max 25 & Bio max 100 chars", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnApplyChanges.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            firestore.collection("users").document(uid)
                .update(mapOf(
                    "username" to username,
                    "bio" to bio
                ))
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    finish() // back to ProfileFragment
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
                    btnApplyChanges.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
        }
    }
}
