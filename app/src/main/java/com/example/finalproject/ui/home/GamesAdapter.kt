package com.example.finalproject.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Game

class GamesAdapter(
    private var games: List<Game>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<GamesAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnGame: ImageButton = itemView.findViewById(R.id.btnGame)
        val tvGameName: TextView = itemView.findViewById(R.id.tvGameName)
    }

    // creates a new view holder for a game item in the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_logo, parent, false)
        return VH(view)
    }

    // binds game data to the view holder at the specified position
    override fun onBindViewHolder(holder: VH, position: Int) {
        val game = games[position]
        holder.btnGame.setImageResource(game.logoRes)
        holder.tvGameName.text = game.name
        holder.btnGame.setOnClickListener { onGameClick(game) }
    }

    // returns the total number of games in the adapter
    override fun getItemCount(): Int = games.size

    // updates the adapter with a new list of games
    fun update(newList: List<Game>) {
        games = newList
        notifyDataSetChanged()
    }
}
