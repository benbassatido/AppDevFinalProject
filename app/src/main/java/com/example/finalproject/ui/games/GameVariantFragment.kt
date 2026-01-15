package com.example.finalproject.ui.games

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.finalproject.R
import com.example.finalproject.data.repository.GameOptionsRepository

class GameVariantFragment : Fragment(R.layout.fragment_game_variant) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameId = requireArguments().getString("gameId") ?: return
        val gameName = requireArguments().getString("gameName").orEmpty()
        val logoRes = requireArguments().getInt("gameLogoRes", 0)

        val ivLogo = view.findViewById<ImageView>(R.id.ivGameLogo)
        if (logoRes != 0) ivLogo.setImageResource(logoRes) else ivLogo.setImageResource(R.drawable.logo_fortnite)

        val container = view.findViewById<LinearLayout>(R.id.optionsContainer)
        container.removeAllViews()
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        val variants = GameOptionsRepository.getVariants(gameId)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        variants.forEachIndexed { index, variant ->

            // OUTER GLOW (same as login)
            val wrapper = FrameLayout(requireContext()).apply {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_outer_glow_primary)
                setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            }

            // BUTTON BODY (same as login)
            val btn = Button(requireContext()).apply {
                text = variant.title
                isAllCaps = true
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                // IMPORTANT: backgroundTint must be null when using a drawable background
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_body_primary)

                minWidth = dpToPx(150)
                minHeight = dpToPx(56)

                setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(16))

                setOnClickListener {
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

            wrapper.addView(btn)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (index != 0) params.marginStart = dpToPx(24)

            wrapper.layoutParams = params
            container.addView(wrapper)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
