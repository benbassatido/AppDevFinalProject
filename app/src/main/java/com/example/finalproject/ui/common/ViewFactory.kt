package com.example.finalproject.ui.common

import android.content.Context
import android.widget.Button
import androidx.core.content.ContextCompat
import com.example.finalproject.R


object ViewFactory {

    // creates a styled button with glow effect and custom properties
    fun buildGlowPillButton(
        context: Context,
        text: String,
        textSize: Float = 15f,
        isAllCaps: Boolean = true,
        minWidth: Int = 110,
        minHeight: Int = 50,
        horizontalPadding: Int = 28,
        verticalPadding: Int = 12,
        onClick: () -> Unit
    ): Button {
        val density = context.resources.displayMetrics.density
        fun dpToPx(dp: Int): Int = (dp * density).toInt()
        
        return Button(context).apply {
            this.text = text
            this.isAllCaps = isAllCaps
            this.textSize = textSize
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            setOnClickListener { onClick() }

            setBackgroundResource(R.drawable.bg_game_option_button)

            this.minWidth = dpToPx(minWidth)
            this.minHeight = dpToPx(minHeight)
            setPadding(
                dpToPx(horizontalPadding),
                dpToPx(verticalPadding),
                dpToPx(horizontalPadding),
                dpToPx(verticalPadding)
            )
        }
    }
}
