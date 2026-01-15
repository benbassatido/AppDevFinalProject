package com.example.finalproject.data.model


data class Room(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var micRequired: Boolean = true,
    var gameId: String = "",
    var gameName: String = "",
    var variant: String = "",
    var partyType: String = "",
    var maxPlayers: Int = 0,
    var ownerUid: String = "",
    var ownerName: String = "",
    var createdAt: Long = 0L,
    var status: String = "open",
    var currentPlayers: Int = 0
)
