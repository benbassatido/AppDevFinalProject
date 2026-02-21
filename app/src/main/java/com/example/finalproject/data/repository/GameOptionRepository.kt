package com.example.finalproject.data.repository

import com.example.finalproject.data.model.GameOption

object GameOptionsRepository {

    private val variantsByGameId: Map<String, List<GameOption>> = mapOf(
        "fortnite" to listOf(
            GameOption("build", "BUILD"),
            GameOption("nobuild", "NO BUILD")
        ),
        "cs2" to listOf(
            GameOption("faceit", "FACEIT"),
            GameOption("premier", "PREMIER")
        ),
        "arc_raiders" to listOf(
            GameOption("dam_battlegrounds", "DAM BATTLEGROUNDS"),
            GameOption("buried_city", "BURIED CITY"),
            GameOption("spaceport", "THE SPACEPORT"),
            GameOption("blue_gate", "BLUE GATE"),
            GameOption("stella_montis", "STELLA MONTIS")
        ),
        "battlefield_6" to listOf(
            GameOption("br", "BATTLE ROYALE"),
            GameOption("mp", "MULTIPLAYER")
        ),
        "cod_bo7" to listOf(
            GameOption("warzone", "WARZONE"),
            GameOption("mp", "MULTIPLAYER")
        ),
        "valorant" to listOf(
            GameOption("iron_bronze", "IRON ~ BRONZE"),
            GameOption("silver_gold", "SILVER ~ GOLD"),
            GameOption("plat_diamond", "PLATINUM ~ DIAMOND"),
            GameOption("immortal", "IMMORTAL"),
            GameOption("radiant", "RADIANT")
        )
    )

    private val optionsByGameAndVariant: Map<String, Map<String, List<GameOption>>> = mapOf(
        "fortnite" to mapOf(
            "build" to listOf(
                GameOption("duo", "DUO"),
                GameOption("trio", "TRIO"),
                GameOption("quad", "QUAD")
            ),
            "nobuild" to listOf(
                GameOption("duo", "DUO"),
                GameOption("trio", "TRIO"),
                GameOption("quad", "QUAD")
            )
        ),
        "cs2" to mapOf(
            "faceit" to listOf(
                GameOption("lvl_1_3", "LEVEL 1 - 3"),
                GameOption("lvl_4_6", "LEVEL 4 - 6"),
                GameOption("lvl_7_9", "LEVEL 7 - 9"),
                GameOption("lvl_10_plus", "LEVEL 10+")
            ),
            "premier" to listOf(
                GameOption("pr_1000_4999", "1,000 ~ 4,999"),
                GameOption("pr_5000_9999", "5,000 ~ 9,999"),
                GameOption("pr_10000_14999", "10,000 ~ 14,999"),
                GameOption("pr_15000_19999", "15,000 ~ 19,999"),
                GameOption("pr_20000_24999", "20,000 ~ 24,999"),
                GameOption("pr_25000_29999", "25,000 ~ 29,999"),
                GameOption("pr_30000_plus", "30,000+")
            )
        ),
        "arc_raiders" to mapOf(
            "dam_battlegrounds" to listOf(GameOption("duo", "DUO"), GameOption("trio", "TRIO")),
            "buried_city" to listOf(GameOption("duo", "DUO"), GameOption("trio", "TRIO")),
            "spaceport" to listOf(GameOption("duo", "DUO"), GameOption("trio", "TRIO")),
            "blue_gate" to listOf(GameOption("duo", "DUO"), GameOption("trio", "TRIO")),
            "stella_montis" to listOf(GameOption("duo", "DUO"), GameOption("trio", "TRIO"))
        ),
        "battlefield_6" to mapOf(
            "br" to listOf(
                GameOption("duo", "DUO"),
                GameOption("trio", "TRIO"),
                GameOption("quad", "QUAD")
            ),
            "mp" to listOf(
                GameOption("rush", "RUSH"),
                GameOption("conquest", "CONQUEST"),
                GameOption("breakthrough", "BREAKTHROUGH"),
                GameOption("domination", "DOMINATION")
            )
        ),
        "cod_bo7" to mapOf(
            "warzone" to listOf(
                GameOption("duo", "DUO"),
                GameOption("trio", "TRIO"),
                GameOption("quad", "QUAD")
            ),
            "mp" to listOf(
                GameOption("snd", "SEARCH AND DESTROY"),
                GameOption("nuketown_247", "NUKETOWN 24/7"),
                GameOption("random", "RANDOM")
            )
        )
    )

    // returns the list of game variants for a specific game
    fun getVariants(gameId: String): List<GameOption> {
        return variantsByGameId[gameId].orEmpty()
    }

    // returns the list of options for a specific game and variant
    fun getOptions(gameId: String, variantId: String): List<GameOption> {
        return optionsByGameAndVariant[gameId]?.get(variantId).orEmpty()
    }

    // determines the maximum number of players for a party type
    fun maxPlayersForPartyType(partyType: String): Int {
        val t = partyType.lowercase()
        return when {
            t.contains("duo") -> 2
            t.contains("trio") -> 3
            t.contains("quad") || t.contains("squad") -> 4
            else -> 0
        }
    }

    // calculates the maximum players for a specific game configuration
    fun getMaxPlayersForGame(gameId: String, variantId: String, partyType: String): Int {
        return when (gameId) {
            "cs2" -> 5
            "cod_bo7" -> 6
            "battlefield_6" -> {
                if (variantId == "mp") {
                    getMaxPlayersForBattlefieldMode(partyType)
                } else {
                    maxPlayersForPartyType(partyType)
                }
            }
            else -> maxPlayersForPartyType(partyType)
        }
    }

    // returns the maximum players for a battlefield multiplayer mode
    private fun getMaxPlayersForBattlefieldMode(partyType: String): Int {
        val mode = partyType.lowercase()
        return when {
            mode.contains("conquest") -> 32
            mode.contains("breakthrough") -> 24
            mode.contains("rush") -> 12
            mode.contains("domination") -> 8
            else -> 0
        }
    }
}
