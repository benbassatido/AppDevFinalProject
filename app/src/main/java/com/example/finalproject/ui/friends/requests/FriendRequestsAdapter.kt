package com.example.finalproject.ui.friends.requests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.User

class FriendRequestsAdapter(
    private val onAccept: (User) -> Unit,
    private val onDecline: (User) -> Unit
) : RecyclerView.Adapter<FriendRequestsAdapter.VH>() {

    private val items = mutableListOf<User>()

    // updates the adapter with a new list of friend requests
    fun submit(list: List<User>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // creates a new view holder for a friend request item in the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        return VH(v)
    }

    // returns the total number of friend requests in the adapter
    override fun getItemCount() = items.size

    // binds friend request data to the view holder at the specified position
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNickname = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        private val btnAccept = itemView.findViewById<Button>(R.id.btnAccept)
        private val btnDecline = itemView.findViewById<Button>(R.id.btnDecline)

        // binds a friend request to the view holder
        fun bind(req: User) {
            tvNickname.text = req.nickname
            tvUsername.text = req.username

            btnAccept.setOnClickListener { onAccept(req) }
            btnDecline.setOnClickListener { onDecline(req) }
        }
    }
}
