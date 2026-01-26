package com.example.finalproject.data.model

data class SearchUserUi(
    val user: User,
    val requestSent: Boolean = false,
    val isFriend: Boolean = false
)
