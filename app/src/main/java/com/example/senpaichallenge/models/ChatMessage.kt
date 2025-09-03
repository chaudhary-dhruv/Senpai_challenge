package com.example.senpaichallenge.models

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val seen: Boolean = false
)
