package com.example.finalproject.ui.rooms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.RoomMember

class RoomMembersAdapter(
    private val myUserKey: String?
) : RecyclerView.Adapter<RoomMembersAdapter.VH>() {

    private val items = mutableListOf<RoomMember>()

    // updates the adapter with a new list of room members
    fun submit(list: List<RoomMember>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // creates a new view holder for a room member item in the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_room_member, parent, false)
        return VH(v)
    }

    // binds room member data to the view holder at the specified position
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], myUserKey)
    }

    // returns the total number of room members in the adapter
    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar = itemView.findViewById<TextView>(R.id.tvAvatar)
        private val tvNickname = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        private val tvMeBadge = itemView.findViewById<TextView>(R.id.tvMeBadge)

        // binds a room member to the view holder with optional me badge
        fun bind(item: RoomMember, myUserKey: String?) {
            tvNickname.text = item.nickname.ifBlank { "Player" }
            tvUsername.text = "@${item.username.ifBlank { "user" }}"

            val letter = (item.nickname.ifBlank { item.username }.trim().firstOrNull()?.uppercaseChar() ?: 'P')
            tvAvatar.text = letter.toString()

            tvMeBadge.visibility = if (myUserKey != null && item.userKey == myUserKey) View.VISIBLE else View.GONE
        }
    }
}
