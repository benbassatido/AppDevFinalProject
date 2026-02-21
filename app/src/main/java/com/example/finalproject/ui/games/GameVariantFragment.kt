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

class GameVariantFragment : Fragment(R.layout.fragment_game_variant) {

    // initializes the game variant selection screen
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameId = requireArguments().getString("gameId") ?: return
        val gameName = requireArguments().getString("gameName").orEmpty()
        val logoRes = requireArguments().getInt("gameLogoRes", 0)

        val directToRooms = requireArguments().getBoolean("directToRooms", false)
        val fixedPartyType = requireArguments().getString("fixedPartyType").orEmpty()
        val fixedMaxPlayers = requireArguments().getInt("fixedMaxPlayers", 0)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val ivLogo = view.findViewById<ImageView>(R.id.ivGameLogo)
        val tvSelectMode = view.findViewById<TextView>(R.id.tvSelectMode)

        val optionsHost = view.findViewById<ConstraintLayout>(R.id.optionsHost)
        val flow = view.findViewById<Flow>(R.id.flowOptions)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        if (logoRes != 0) ivLogo.setImageResource(logoRes) else ivLogo.setImageResource(R.drawable.logo_fortnite)

        tvSelectMode.text = when (gameId) {
            "arc_raiders" -> "SELECT A MAP"
            "valorant" -> "SELECT A RANK"
            else -> "SELECT A MODE"
        }

        optionsHost.removeAllViews()
        optionsHost.addView(flow)

        val variants = GameOptionsRepository.getVariants(gameId)
        val ids = ArrayList<Int>(variants.size)

        variants.forEach { variant ->
            val pill = buildGlowPillButton(variant.title) {
                if (directToRooms) {
                    val partyType = if (fixedPartyType.isNotBlank()) fixedPartyType else "Competitive"
                    val maxPlayers = if (fixedMaxPlayers > 0) fixedMaxPlayers else 5

                    val next = RoomsFragment().apply {
                        arguments = bundleOf(
                            "gameId" to gameId,
                            "gameName" to gameName,
                            "variant" to variant.title,
                            "partyType" to partyType,
                            "maxPlayers" to maxPlayers
                        )
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, next)
                        .addToBackStack(null)
                        .commit()
                } else {
                    val next = GameDetailsFragment().apply {
                        arguments = bundleOf(
                            "gameId" to gameId,
                            "gameName" to gameName,
                            "gameLogoRes" to logoRes,
                            "variantId" to variant.id,
                            "variantTitle" to variant.title
                        )
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, next)
                        .addToBackStack(null)
                        .commit()
                }
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

    // creates a styled button for variant selection
    private fun buildGlowPillButton(text: String, onClick: () -> Unit) =
        ViewFactory.buildGlowPillButton(
            context = requireContext(),
            text = text,
            onClick = onClick
        )
}
