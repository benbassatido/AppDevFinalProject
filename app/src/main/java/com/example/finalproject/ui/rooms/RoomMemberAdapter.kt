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

    fun submit(list: List<RoomMember>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_room_member, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], myUserKey)
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar = itemView.findViewById<TextView>(R.id.tvAvatar)
        private val tvNickname = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        private val tvMeBadge = itemView.findViewById<TextView>(R.id.tvMeBadge)

        fun bind(item: RoomMember, myUserKey: String?) {
            tvNickname.text = item.nickname.ifBlank { "Player" }
            tvUsername.text = "@${item.username.ifBlank { "user" }}"

            val letter = (item.nickname.ifBlank { item.username }.trim().firstOrNull()?.uppercaseChar() ?: 'P')
            tvAvatar.text = letter.toString()

            tvMeBadge.visibility = if (myUserKey != null && item.userKey == myUserKey) View.VISIBLE else View.GONE
        }
    }
}
