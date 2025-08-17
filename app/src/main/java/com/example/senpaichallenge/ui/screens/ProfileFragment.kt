package com.example.senpaichallenge.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.senpaichallenge.R
import com.example.senpaichallenge.ui.auth.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        // 🔹 SharedPreferences se user data nikalna
        val prefs = requireActivity().getSharedPreferences("UserData", android.content.Context.MODE_PRIVATE)
        val email = prefs.getString("email", "No Email")
        val name = prefs.getString("name", "No Name")
        val avatarName = prefs.getString("avatar", "avatar1")

        // 🔹 Avatar set karna
        val avatarResId = resources.getIdentifier(avatarName, "drawable", requireContext().packageName)
        view.findViewById<CircleImageView>(R.id.imgUserAvatar).apply {
            if (avatarResId != 0) setImageResource(avatarResId)
            else setImageResource(R.drawable.avatar1)
        }

        // 🔹 Username set karna
        view.findViewById<TextView>(R.id.tvUsername).text = name ?: "Guest"

        // 🔹 Dummy ranking aur points (baad me Firestore se load karenge)
        view.findViewById<TextView>(R.id.tvRank).text = "5,678"
        view.findViewById<TextView>(R.id.tvPoints).text = "100k"

        // 🔹 Settings icon → abhi ke liye SignOut ka kaam karega
        view.findViewById<View>(R.id.btnSettings).setOnClickListener {
            auth.signOut()
            prefs.edit().clear().apply()
            Toast.makeText(requireContext(), "Signed out!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
            requireActivity().finish()
        }

        // 🔹 Spinner (dropdown anime filter)
        val spinner: Spinner = view.findViewById(R.id.pointsFilterSpinner)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.points_filter_options,
            R.layout.spinner_item   // 👈 Custom item layout
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // 👈 Dropdown style
            spinner.adapter = adapter
        }

        spinner.setSelection(0) // Default "All" select hoga

        return view
    }
}
