package com.example.finalproject.data.repository

data class GameOption(
    val id: String,
    val title: String
)

object GameOptionsRepository {

    fun getVariants(gameId: String): List<GameOption> {
        return when (gameId) {
            "fortnite" -> listOf(
                GameOption("build", "BUILD"),
                GameOption("nobuild", "NO BUILD")
            )
            else -> emptyList()
        }
    }

    fun getOptions(gameId: String): List<GameOption> {
        return when (gameId) {
            "fortnite" -> listOf(
                GameOption("duo", "DUO"),
                GameOption("trio", "TRIO"),
                GameOption("quad", "QUAD")
            )
            else -> emptyList()
        }
    }
}

