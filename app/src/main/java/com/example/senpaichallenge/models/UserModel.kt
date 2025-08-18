package com.example.senpaichallenge.models


data class UserModel(
    val username: String,
    val animeId: String,
    val avatar: String,
    val points: Int,
    val bio: String = ""
)
