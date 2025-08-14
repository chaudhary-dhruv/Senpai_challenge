package com.example.senpaichallenge.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.senpaichallenge.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var strEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        mAuth = FirebaseAuth.getInstance()

        // Reset Button Listener
        binding.btnSendLink.setOnClickListener {
            strEmail = binding.emailInput.text.toString().trim()
            if (strEmail.isNotEmpty()) {
                resetPassword()
            } else {
                binding.emailInput.error = "Email field can't be empty"
            }
        }

        // Back Arrow
        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Redirect to login link
        binding.redirectLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun resetPassword() {
        // Agar tum progress bar add karoge to use show kar sakte ho:
        // binding.progressBar.visibility = View.VISIBLE
        // binding.btnSendLink.visibility = View.INVISIBLE

        mAuth.sendPasswordResetEmail(strEmail)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Reset Password link has been sent to your registered Email",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                // Agar tum progress bar lagate ho to wapas hide karo:
                // binding.progressBar.visibility = View.INVISIBLE
                // binding.btnSendLink.visibility = View.VISIBLE
            }
    }
}
