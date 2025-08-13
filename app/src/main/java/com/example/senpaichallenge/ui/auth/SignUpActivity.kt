package com.example.senpaichallenge.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.senpaichallenge.R
import androidx.appcompat.app.AppCompatDelegate

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}