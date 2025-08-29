package com.example.senpaichallenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.senpaichallenge.databinding.ActivityMainBinding
import com.example.senpaichallenge.ui.screens.*
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Fragments ko ek hi baar create karenge
    private lateinit var homeFragment: HomeFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var leaderboardFragment: LeaderboardFragment
    private lateinit var profileFragment: ProfileFragment

    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Always use dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Fragments initialize
        homeFragment = HomeFragment()
        chatFragment = ChatFragment()
        leaderboardFragment = LeaderboardFragment()
        profileFragment = ProfileFragment()

        // Sab fragments add karo lekin ek hi visible rahe
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, homeFragment, "HOME")
            .commit()
        activeFragment = homeFragment

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, chatFragment, "CHAT")
            .hide(chatFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, leaderboardFragment, "LEADERBOARD")
            .hide(leaderboardFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, profileFragment, "PROFILE")
            .hide(profileFragment)
            .commit()

        // Agar openProfile flag true hai to ProfileFragment load hoga
        if (intent.getBooleanExtra("openProfile", false)) {
            switchFragment(profileFragment)
            binding.bottomNav.selectedItemId = R.id.nav_profile
        }

        // Bottom nav listener
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(homeFragment)
                R.id.nav_chat -> switchFragment(chatFragment)
                R.id.nav_leaderboard -> switchFragment(leaderboardFragment)
                R.id.nav_profile -> switchFragment(profileFragment)
            }
            true
        }
    }

    private fun switchFragment(target: Fragment) {
        if (target == activeFragment) return // agar same fragment hai to kuch mat karo

        val transaction = supportFragmentManager.beginTransaction()
        activeFragment?.let { transaction.hide(it) }
        transaction.show(target).commit()
        activeFragment = target
    }
}