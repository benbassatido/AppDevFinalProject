package com.example.finalproject.ui.games

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.finalproject.R
import com.example.finalproject.data.repository.GameOptionsRepository
import com.example.finalproject.ui.rooms.RoomsFragment
import com.google.android.material.button.MaterialButton

class GameDetailsFragment : Fragment(R.layout.fragment_game_details) {

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
        if (gameId == "cs2") return 5
        if (gameId == "cod_bo7") return 6

        if (gameId == "battlefield_6") {
            val t = partyType.lowercase()
            return if (variantId == "mp") {
                when {
                    t.contains("conquest") -> 32
                    t.contains("breakthrough") -> 24
                    t.contains("rush") -> 12
                    t.contains("domination") -> 8
                    else -> 0
                }
            } else {
                when {
                    t.contains("duo") -> 2
                    t.contains("trio") -> 3
                    t.contains("quad") || t.contains("squad") || t.contains("quads") || t.contains("squads") -> 4
                    else -> 0
                }
            }
        }

        val t = partyType.lowercase()
        return when {
            t.contains("duo") -> 2
            t.contains("trio") -> 3
            t.contains("quad") || t.contains("squad") || t.contains("quads") || t.contains("squads") -> 4
            else -> 0
        }
    }

    private fun buildGlowPillButton(text: String, onClick: () -> Unit): FrameLayout {
        val wrap = FrameLayout(requireContext()).apply {
            setBackgroundResource(R.drawable.bg_btn_outer_glow_primary)
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            foregroundGravity = Gravity.CENTER
        }

        val btn = MaterialButton(requireContext()).apply {
            this.text = text
            isAllCaps = false
            textSize = 13f
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { onClick() }

            backgroundTintList = null
            setBackgroundResource(R.drawable.bg_btn_body_primary)

            minHeight = dpToPx(44)
            setPadding(dpToPx(18), dpToPx(10), dpToPx(18), dpToPx(10))
            elevation = 0f
        }

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            dpToPx(44)
        )
        lp.gravity = Gravity.CENTER
        btn.layoutParams = lp

        wrap.addView(btn)
        return wrap
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
