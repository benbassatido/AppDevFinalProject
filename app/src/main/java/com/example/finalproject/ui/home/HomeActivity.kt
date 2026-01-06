package com.example.finalproject.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Game
import com.example.finalproject.ui.friends.FriendsActivity
import com.example.finalproject.ui.games.GameDetailsActivity
import com.example.finalproject.ui.games.GameVariantActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: GamesAdapter
    private lateinit var allGames: List<Game>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvHello = findViewById<TextView>(R.id.tvHelloUser)
        val username = intent.getStringExtra("username") ?: "Player"
        tvHello.text = "Hello $username"

        val rv = findViewById<RecyclerView>(R.id.rvGames)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        rv.layoutManager = GridLayoutManager(this, 3)

        allGames = listOf(
            Game("fortnite", "Fortnite", R.drawable.logo_fortnite),
            Game("cs2", "CS2", R.drawable.logo_cs2),
            Game("arc_riders", "Arc Riders", R.drawable.logo_arc_riders),
            Game("battlefield_6", "Battlefield 6", R.drawable.logo_battlefield_6),
            Game("cod_bo7", "COD Black Ops 7", R.drawable.logo_cod_black_ops),
            Game("valorant", "Valorant", R.drawable.logo_valorant)
        )

        adapter = GamesAdapter(allGames) { game ->
            val gameId = game.name.trim().lowercase()
            val intent = if (game.id == "fortnite") {
                Intent(this, GameVariantActivity::class.java)
            } else {
                Intent(this, GameDetailsActivity::class.java)
            }

            intent.putExtra("gameId", gameId)
            intent.putExtra("gameName", game.name)
            intent.putExtra("gameLogoRes", game.logoRes)
            startActivity(intent)
        }
        rv.adapter = adapter


        // VIEW FRIENDS
        findViewById<MaterialButton>(R.id.btnViewFriends).setOnClickListener {
            startActivity(Intent(this, FriendsActivity::class.java))
        }

        // SEARCH FILTER
        etSearch.doAfterTextChanged { text ->
            val q = text?.toString()?.trim().orEmpty().lowercase()
            val filtered =
                if (q.isEmpty()) allGames
                else allGames.filter { it.name.lowercase().contains(q) }

            adapter.update(filtered)
        }
    }
}
