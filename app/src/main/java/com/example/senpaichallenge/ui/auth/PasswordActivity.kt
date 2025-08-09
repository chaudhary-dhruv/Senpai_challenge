package com.example.senpaichallenge.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.senpaichallenge.R
import androidx.appcompat.app.AppCompatDelegate

class PasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
