package com.example.finalproject.ui.games

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finalproject.R
import com.example.finalproject.data.repository.GameOptionsRepository
import com.example.finalproject.ui.rooms.RoomsActivity

class GameDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_details)

        val gameId = intent.getStringExtra("gameId") ?: return

        val ivLogo = findViewById<ImageView>(R.id.ivGameLogo)
        val logoRes = intent.getIntExtra("gameLogoRes", 0)
        if (logoRes != 0) {
            ivLogo.setImageResource(logoRes)
        } else {
            ivLogo.setImageResource(R.drawable.logo_fortnite)
        }

        val container = findViewById<LinearLayout>(R.id.optionsContainer)
        container.removeAllViews()

        val options = GameOptionsRepository.getOptions(gameId)

        options.forEachIndexed { index, option ->
            val button = Button(this).apply {
                text = option.title
                background = ContextCompat.getDrawable(this@GameDetailsActivity, R.drawable.btn_mode_black)
                setTextColor(ContextCompat.getColor(this@GameDetailsActivity, R.color.sky_blue))
                isAllCaps = true
                textSize = 12f

                setOnClickListener {
                    val i = Intent(this@GameDetailsActivity, RoomsActivity::class.java)
                    i.putExtra("gameId", gameId)
                    i.putExtra("modeId", option.id)       // "duo" / "trio" / "quad"
                    i.putExtra("modeTitle", option.title) // "DUO" / "TRIO" / "QUAD"
                    startActivity(i)
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            if (index != 0) params.marginStart = dpToPx(18)

            button.layoutParams = params
            container.addView(button)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
