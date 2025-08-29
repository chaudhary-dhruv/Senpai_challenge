package com.example.senpaichallenge.ui.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.senpaichallenge.R
import com.example.senpaichallenge.databinding.ActivityLoginBinding
import com.example.senpaichallenge.utils.UiUtils.toggleLoadingButton
import com.example.senpaichallenge.utils.UiUtils.toggleLoadingGoogle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back - Arrow
        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, ChoiceActivity::class.java))
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // Google Sign-In Config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
        googleSignInClient.revokeAccess()

        // Sign Up link
        binding.registerClickable.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        // Forgot Password link
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
            finish()
        }

        // Email/Password Login
        binding.btnLogin.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val pass = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                toggleLoadingButton(binding.btnLogin, binding.progressBar, true, "Continue")
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    toggleLoadingButton(binding.btnLogin, binding.progressBar, false, "Continue")
                    if (it.isSuccessful) {
                        goToNextStep()
                    } else {
                        Toast.makeText(
                            this,
                            it.exception?.message ?: "Login Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In Button
        val googleBtn = findViewById<LinearLayout>(R.id.googleSignInButton)
        googleBtn.setOnClickListener {
            toggleLoadingGoogle(googleBtn, binding.progressBar, true)
            signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            toggleLoadingGoogle(findViewById(R.id.googleSignInButton), binding.progressBar, false)
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            toggleLoadingGoogle(findViewById(R.id.googleSignInButton), binding.progressBar, false)
            if (it.isSuccessful) {
                goToNextStep()
            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToNextStep() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .update("fcmToken", token)
            }
        }

        val intent = Intent(this, SplashScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
