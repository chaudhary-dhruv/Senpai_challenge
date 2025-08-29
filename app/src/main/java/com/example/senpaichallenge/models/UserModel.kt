package com.example.senpaichallenge.models

data class UserModel(
    val uid: String = "",         // 🔹 unique Firebase UID (chat & identity)
    val username: String = "",
    val animeId: String = "",
    val avatar: String = "",
    val points: Int = 0,
    val bio: String = ""
)
