package com.example.finalproject.ui.games

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.finalproject.R
import com.example.finalproject.data.repository.GameOptionsRepository
import com.example.finalproject.ui.common.ViewFactory
import com.example.finalproject.ui.rooms.RoomsFragment

class GameDetailsFragment : Fragment(R.layout.fragment_game_details) {

    companion object {
        private const val CS2_TEAM_SIZE = 5
        private const val COD_TEAM_SIZE = 6
        private const val CONQUEST_PLAYERS = 32
        private const val BREAKTHROUGH_PLAYERS = 24
        private const val RUSH_PLAYERS = 12
        private const val DOMINATION_PLAYERS = 8
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameId = requireArguments().getString("gameId") ?: return
        val gameName = requireArguments().getString("gameName").orEmpty()
        val logoRes = requireArguments().getInt("gameLogoRes", 0)
        val variantId = requireArguments().getString("variantId").orEmpty()
        val variantTitle = requireArguments().getString("variantTitle").orEmpty()

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val ivLogo = view.findViewById<ImageView>(R.id.ivGameLogo)
        val optionsHost = view.findViewById<ConstraintLayout>(R.id.optionsHost)
        val flow = view.findViewById<Flow>(R.id.flowOptions)
        val tvSelectMode = view.findViewById<TextView>(R.id.tvSelectMode)

        tvSelectMode.text = when (gameId) {
            "cs2" -> "SELECT RANK TO FIND ROOMS"
            "arc_riders" -> "SELECT A TEAM SIZE"
            "battlefield_6" -> if (variantId == "mp") "SELECT MODE TO FIND ROOMS" else "SELECT TEAM SIZE"
            "cod_bo7" -> if (variantId == "mp") "SELECT MODE TO FIND ROOMS" else "SELECT TEAM SIZE"
            else -> "SELECT SIZE SQUAD TO FIND ROOMS"
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        if (logoRes != 0) ivLogo.setImageResource(logoRes) else ivLogo.setImageResource(R.drawable.logo_fortnite)

        optionsHost.removeAllViews()
        optionsHost.addView(flow)

        val options = GameOptionsRepository.getOptions(gameId, variantId)
        val ids = ArrayList<Int>(options.size)

        options.forEach { option ->
            val pill = buildGlowPillButton(option.title) {
                val partyType = option.title
                val maxPlayers = maxPlayersFor(gameId, variantId, partyType)

                val next = RoomsFragment().apply {
                    arguments = bundleOf(
                        "gameId" to gameId,
                        "gameName" to gameName,
                        "variant" to variantTitle,
                        "partyType" to partyType,
                        "maxPlayers" to maxPlayers
                    )
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, next)
                    .addToBackStack(null)
                    .commit()
            }

            pill.id = View.generateViewId()
            ids.add(pill.id)

            optionsHost.addView(
                pill,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        flow.referencedIds = ids.toIntArray()
    }

    private fun maxPlayersFor(gameId: String, variantId: String, partyType: String): Int {
        if (gameId == "cs2") return CS2_TEAM_SIZE
        if (gameId == "cod_bo7") return COD_TEAM_SIZE

        if (gameId == "battlefield_6") {
            val t = partyType.lowercase()
            return if (variantId == "mp") {
                when {
                    t.contains("conquest") -> CONQUEST_PLAYERS
                    t.contains("breakthrough") -> BREAKTHROUGH_PLAYERS
                    t.contains("rush") -> RUSH_PLAYERS
                    t.contains("domination") -> DOMINATION_PLAYERS
                    else -> 0
                }
            } else {
                GameOptionsRepository.maxPlayersForPartyType(partyType)
            }
        }

        return GameOptionsRepository.maxPlayersForPartyType(partyType)
    }

    private fun buildGlowPillButton(text: String, onClick: () -> Unit) =
        ViewFactory.buildGlowPillButton(
            context = requireContext(),
            text = text,
            textSize = 13f,
            isAllCaps = false,
            minWidth = 0,
            minHeight = 44,
            horizontalPadding = 18,
            verticalPadding = 10,
            onClick = onClick
        )
}
