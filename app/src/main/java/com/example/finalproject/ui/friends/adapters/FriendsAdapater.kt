package com.example.finalproject.ui.friends.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Friend
import com.google.android.material.card.MaterialCardView

class FriendsAdapter(
    private val onFriendClick: (Friend) -> Unit,
    private val onDeleteClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.VH>() {

    private val items = mutableListOf<Friend>()

    fun submit(list: List<Friend>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeByUid(uid: String) {
        val idx = items.indexOfFirst { it.uid == uid }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun currentList(): List<Friend> = items.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return VH(v, onFriendClick, onDeleteClick)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(
        itemView: View,
        private val onFriendClick: (Friend) -> Unit,
        private val onDeleteClick: (Friend) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val card = itemView.findViewById<MaterialCardView>(R.id.cardFriend)
        private val tvNickname = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        private val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDeleteFriend)

        fun bind(f: Friend) {
            tvNickname.text = f.nickname
            tvUsername.text = f.username

            card.setOnClickListener { onFriendClick(f) }
            btnDelete.setOnClickListener { onDeleteClick(f) }
        }
    }
}
