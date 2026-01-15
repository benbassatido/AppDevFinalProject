package com.example.finalproject.ui.games

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
        val variantTitle = requireArguments().getString("variantTitle").orEmpty()
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)





        val ivLogo = view.findViewById<ImageView>(R.id.ivGameLogo)
        if (logoRes != 0) ivLogo.setImageResource(logoRes) else ivLogo.setImageResource(R.drawable.logo_fortnite)

        val container = view.findViewById<LinearLayout>(R.id.optionsContainer)
        container.removeAllViews()

        val options = GameOptionsRepository.getOptions(gameId)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        options.forEachIndexed { index, option ->
            val pill = buildGlowPillButton(option.title) {
                val partyType = option.title
                val maxPlayers = partyTypeToMaxPlayers(partyType)

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

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (index != 0) params.marginStart = dpToPx(14)
            pill.layoutParams = params

            container.addView(pill)
        }
    }

    private fun buildGlowPillButton(text: String, onClick: () -> Unit): FrameLayout {
        val wrap = FrameLayout(requireContext()).apply {
            // same glow background you already use in login/register
            setBackgroundResource(R.drawable.bg_btn_outer_glow_primary)
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            foregroundGravity = Gravity.CENTER
        }

        val btn = MaterialButton(requireContext()).apply {
            this.text = text
            isAllCaps = true
            textSize = 13f
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { onClick() }

            backgroundTintList = null
            setBackgroundResource(R.drawable.bg_btn_body_primary)

            minWidth = dpToPx(96)
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

    private fun partyTypeToMaxPlayers(partyType: String): Int {
        val t = partyType.lowercase()
        return when {
            t.contains("duo") -> 2
            t.contains("trio") -> 3
            t.contains("squad") || t.contains("squads") -> 4
            else -> 0
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
