package com.example.senpaichallenge.models

data class ChatUser(
    val userId: String = "",
    val username: String = "",
    val animeId: String = "",
    val avatar: String = "",
    val lastMessage: String = "",
    val lastSeen: Long = 0L,
    val unreadCount: Int = 0
)
