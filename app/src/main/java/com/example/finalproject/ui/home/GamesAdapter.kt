package com.example.finalproject.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Game

class GamesAdapter(
    private var games: List<Game>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<GamesAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnGame: ImageButton = itemView.findViewById(R.id.btnGame)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_logo, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val game = games[position]
        holder.btnGame.setImageResource(game.logoRes)
        holder.btnGame.setOnClickListener { onGameClick(game) }
    }

    override fun getItemCount(): Int = games.size

    fun update(newList: List<Game>) {
        games = newList
        notifyDataSetChanged()
    }
}
