package com.example.finalproject.ui.common

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.finalproject.R
import com.google.android.material.button.MaterialButton


object ViewFactory {

    fun buildGlowPillButton(
        context: Context,
        text: String,
        textSize: Float = 14f,
        isAllCaps: Boolean = true,
        minWidth: Int = 150,
        minHeight: Int = 56,
        horizontalPadding: Int = 24,
        verticalPadding: Int = 16,
        onClick: () -> Unit
    ): FrameLayout {
        val density = context.resources.displayMetrics.density
        fun dpToPx(dp: Int): Int = (dp * density).toInt()
        
        val wrap = FrameLayout(context).apply {
            setBackgroundResource(R.drawable.bg_btn_outer_glow_primary)
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            foregroundGravity = Gravity.CENTER
        }

        val btn = MaterialButton(context).apply {
            this.text = text
            this.isAllCaps = isAllCaps
            this.textSize = textSize
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setOnClickListener { onClick() }

            backgroundTintList = null
            setBackgroundResource(R.drawable.bg_btn_body_primary)

            this.minWidth = dpToPx(minWidth)
            this.minHeight = dpToPx(minHeight)
            setPadding(
                dpToPx(horizontalPadding),
                dpToPx(verticalPadding),
                dpToPx(horizontalPadding),
                dpToPx(verticalPadding)
            )

            elevation = 0f
        }

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            dpToPx(minHeight)
        )
        lp.gravity = Gravity.CENTER
        btn.layoutParams = lp

        wrap.addView(btn)
        return wrap
    }
}
