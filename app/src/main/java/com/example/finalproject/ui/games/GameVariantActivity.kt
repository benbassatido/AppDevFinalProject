package com.example.finalproject.ui.games

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finalproject.R
import com.example.finalproject.data.repository.GameOptionsRepository

class GameVariantActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_variant)

        val gameId = intent.getStringExtra("gameId") ?: return
        val logoRes = intent.getIntExtra("gameLogoRes", 0)

        val ivLogo = findViewById<ImageView>(R.id.ivGameLogo)
        if (logoRes != 0) ivLogo.setImageResource(logoRes) else ivLogo.setImageResource(R.drawable.logo_fortnite)

        val container = findViewById<LinearLayout>(R.id.optionsContainer)
        container.removeAllViews()

        val variants = GameOptionsRepository.getVariants(gameId)

        variants.forEachIndexed { index, variant ->
            val btn = Button(this).apply {
                text = variant.title
                background = ContextCompat.getDrawable(this@GameVariantActivity, R.drawable.btn_mode_black)
                setTextColor(ContextCompat.getColor(this@GameVariantActivity, R.color.sky_blue))
                isAllCaps = true
                textSize = 14f
                minWidth = dpToPx(150)
                minHeight = dpToPx(64)
                setPadding(dpToPx(22), dpToPx(14), dpToPx(22), dpToPx(14))

                setOnClickListener {
                    val i = Intent(this@GameVariantActivity, GameDetailsActivity::class.java)
                    i.putExtra("gameId", gameId)
                    i.putExtra("gameLogoRes", logoRes)
                    i.putExtra("variantId", variant.id) // build / nobuild
                    startActivity(i)
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (index != 0) params.marginStart = dpToPx(24)
            btn.layoutParams = params

            container.addView(btn)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
