package com.example.finalproject.data.model


data class SearchUserUi(
    val user: User,
    var requestSent: Boolean = false,
    var isFriend: Boolean = false
)
