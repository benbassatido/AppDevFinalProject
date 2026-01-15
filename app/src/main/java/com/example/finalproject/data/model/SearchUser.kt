package com.example.finalproject.data.model

data class SearchUser(
    val uid: String = "",
    val username: String = "",
    val nickname: String = "",
    val nicknameLower: String = "",
    var requestSent: Boolean = false,
    var isFriend: Boolean = false
)
