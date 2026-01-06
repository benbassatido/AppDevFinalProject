package com.example.finalproject.ui.rooms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R

class RoomsAdapter(
    private var rooms: List<String>,
    private val onRoomClick: (String) -> Unit
) : RecyclerView.Adapter<RoomsAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val room = rooms[position]
        holder.tvRoomName.text = room
        holder.itemView.setOnClickListener { onRoomClick(room) }
    }

    override fun getItemCount(): Int = rooms.size

    fun update(newRooms: List<String>) {
        rooms = newRooms
        notifyDataSetChanged()
    }
}
