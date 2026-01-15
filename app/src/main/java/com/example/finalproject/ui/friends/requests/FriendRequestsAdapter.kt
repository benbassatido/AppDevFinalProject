package com.example.finalproject.ui.friends.requests

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Friend

class FriendRequestsAdapter(
    private val onAccept: (Friend) -> Unit,
    private val onDecline: (Friend) -> Unit
) : RecyclerView.Adapter<FriendRequestsAdapter.VH>() {

    private val items = mutableListOf<Friend>()

    fun submit(list: List<Friend>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNickname = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        private val btnAccept = itemView.findViewById<Button>(R.id.btnAccept)
        private val btnDecline = itemView.findViewById<Button>(R.id.btnDecline)

        fun bind(req: Friend) {
            tvNickname.text = req.nickname
            tvUsername.text = req.username

            btnAccept.setOnClickListener { onAccept(req) }
            btnDecline.setOnClickListener { onDecline(req) }
        }
    }
}
