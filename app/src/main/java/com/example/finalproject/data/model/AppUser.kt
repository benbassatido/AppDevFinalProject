package com.example.finalproject.data.model

data class AppUser(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val nickname: String = "",
    val nicknameLower: String = "",
    val createdAt: Long = 0L
)
