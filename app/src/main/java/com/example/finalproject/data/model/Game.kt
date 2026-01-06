package com.example.finalproject.data.model

import androidx.annotation.DrawableRes

data class Game(
    val id: String,
    val name: String,
    @DrawableRes val logoRes: Int
)
