package com.example.finalproject.data.repository

import com.example.finalproject.R
import com.example.finalproject.data.model.Game

object GamesRepository {
    
    fun getAllGames(): List<Game> = listOf(
        Game("fortnite", "Fortnite", R.drawable.logo_fortnite),
        Game("cs2", "Counter Strike 2", R.drawable.logo_cs2),
        Game("arc_riders", "Arc Riders", R.drawable.logo_arc_riders),
        Game("battlefield_6", "Battlefield 6", R.drawable.logo_battlefield_6),
        Game("cod_bo7", "COD Black Ops 7", R.drawable.logo_cod_black_ops),
        Game("valorant", "Valorant", R.drawable.logo_valorant)
    )
    
    fun searchGames(query: String): List<Game> {
        if (query.isBlank()) return getAllGames()
        val lowerQuery = query.lowercase()
        return getAllGames().filter { it.name.lowercase().contains(lowerQuery) }
    }
}
