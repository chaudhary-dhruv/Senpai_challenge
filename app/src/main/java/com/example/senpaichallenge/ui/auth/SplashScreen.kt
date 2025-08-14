package com.example.senpaichallenge.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.senpaichallenge.MainActivity
import com.example.senpaichallenge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreen : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        checkUserStatus()
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User logged in -> check profile completed or not
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Profile complete -> MainActivity
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // Profile incomplete -> DetailActivity
                        startActivity(Intent(this, DetailActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    // Error case -> back to login
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        } else {
            // User not logged in -> show choice page
            startActivity(Intent(this, ChoiceActivity::class.java))
            finish()
        }
    }
}
