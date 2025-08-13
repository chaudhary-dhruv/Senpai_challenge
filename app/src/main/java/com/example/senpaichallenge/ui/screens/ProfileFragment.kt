package com.example.senpaichallenge.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.senpaichallenge.R
import com.example.senpaichallenge.ui.auth.SignUpActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val prefs = requireActivity().getSharedPreferences("UserData", AppCompatActivity.MODE_PRIVATE)
        val email = prefs.getString("email", "No Email")
        val name = prefs.getString("name", "No Name")

        view.findViewById<TextView>(R.id.textViewProfile).text = "$name\n$email"

        view.findViewById<Button>(R.id.signOutBtn).setOnClickListener {
            auth.signOut()
            prefs.edit().clear().apply()
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
            requireActivity().finish()
        }

        return view
    }
}
