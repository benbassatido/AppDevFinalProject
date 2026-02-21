package com.example.finalproject.ui.rooms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Room
import com.google.android.material.button.MaterialButton

class RoomsAdapter(
    private val onViewClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomsAdapter.RoomVH>() {

    private val items = mutableListOf<Room>()

    private var currentRoomId: String? = null

    // updates the adapter with a new list of rooms
    fun submitList(list: List<Room>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // sets the current room id for highlighting joined rooms
    fun setCurrentRoomId(roomId: String?) {
        currentRoomId = roomId
        notifyDataSetChanged()
    }

    // creates a new view holder for a room item in the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomVH(v)
    }

    // binds room data to the view holder at the specified position
    override fun onBindViewHolder(holder: RoomVH, position: Int) {
        holder.bind(items[position])
    }

    // returns the total number of rooms in the adapter
    override fun getItemCount(): Int = items.size

    inner class RoomVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle = itemView.findViewById<TextView>(R.id.tvRoomTitle)
        private val tvSub = itemView.findViewById<TextView>(R.id.tvRoomSub)
        private val tvDesc = itemView.findViewById<TextView>(R.id.tvRoomDesc)
        private val btn = itemView.findViewById<MaterialButton>(R.id.btnJoinRoom)

        // binds a room to the view holder with join status
        fun bind(room: Room) {
            tvTitle.text = room.title.ifBlank { "Room" }
            tvSub.text =
                "${room.variant.uppercase()} • ${room.partyType.uppercase()} • ${room.currentPlayers}/${room.maxPlayers}"
            tvDesc.text = room.description.ifBlank { "No description" }

            val isJoined = !currentRoomId.isNullOrBlank() && currentRoomId == room.id

            btn.text = if (isJoined) "JOINED" else "VIEW ROOM"

            btn.alpha = if (isJoined) 0.8f else 1f

            btn.setOnClickListener {
                onViewClick(room)
            }

        }
    }
}
