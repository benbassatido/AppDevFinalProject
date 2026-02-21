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

    // updates the adapter with a new list of friends
    fun submit(list: List<Friend>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // removes a friend from the list by user key
    fun removeByUid(userKey: String) {
        val idx = items.indexOfFirst { it.userKey == userKey }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    // returns the current list of friends
    fun currentList(): List<Friend> = items.toList()

    // creates a new view holder for a friend item in the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return VH(v, onFriendClick, onDeleteClick)
    }

    // returns the total number of friends in the adapter
    override fun getItemCount() = items.size

    // binds friend data to the view holder at the specified position
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

        // binds a friend to the view holder with click handlers
        fun bind(f: Friend) {
            tvNickname.text = f.nickname
            tvUsername.text = f.username

            card.setOnClickListener { onFriendClick(f) }
            btnDelete.setOnClickListener { onDeleteClick(f) }
        }
    }
}
