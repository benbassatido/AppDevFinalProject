package com.example.finalproject.data.model

data class Room(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val micRequired: Boolean = true,
    val gameId: String = "",
    val gameName: String = "",
    val variant: String = "",
    val partyType: String = "",
    val maxPlayers: Int = 0,
    val ownerUid: String = "",
    val ownerName: String = "",
    val createdAt: Long = 0L,
    val status: String = "open",
    val currentPlayers: Int = 0
)
