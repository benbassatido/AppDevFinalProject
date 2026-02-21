package com.example.finalproject.ui.chat

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.ChatMessage
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(
    private val myUserKey: String,
    private val otherNickname: String
) : RecyclerView.Adapter<MessagesAdapter.VH>() {

    private val items = mutableListOf<ChatMessage>()
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    // updates the adapter with a new list of chat messages
    fun submit(list: List<ChatMessage>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // creates a new view holder for a message item in the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    // returns the total number of messages in the adapter
    override fun getItemCount() = items.size

    // binds message data to the view holder at the specified position
    override fun onBindViewHolder(holder: VH, position: Int) {
        val prev = if (position > 0) items[position - 1] else null
        holder.bind(items[position], prev, myUserKey, otherNickname, timeFmt)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageContainer = itemView.findViewById<LinearLayout>(R.id.messageContainer)
        private val bubbleContainer = itemView.findViewById<MaterialCardView>(R.id.bubbleContainer)
        private val tvSender = itemView.findViewById<TextView>(R.id.tvSender)
        private val tvText = itemView.findViewById<TextView>(R.id.tvText)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        // binds a chat message to the view holder with appropriate styling
        fun bind(
            msg: ChatMessage,
            prev: ChatMessage?,
            myUserKey: String,
            otherNickname: String,
            timeFmt: SimpleDateFormat
        ) {
            val isMine = msg.senderId == myUserKey
            val prevSameSender = prev?.senderId == msg.senderId

            // Show sender name only for first message in a group
            if (!prevSameSender) {
                tvSender.visibility = View.VISIBLE
                tvSender.text = if (isMine) "You" else otherNickname
            } else {
                tvSender.visibility = View.GONE
            }

            tvText.text = msg.text
            tvTime.text = timeFmt.format(Date(msg.createdAt))

            // Position the message container
            val frameParams = messageContainer.layoutParams as FrameLayout.LayoutParams
            
            if (isMine) {
                // My messages
                frameParams.gravity = Gravity.END
                bubbleContainer.setCardBackgroundColor(0xFF8BA7F5.toInt())
                tvText.setTextColor(0xFFFFFFFF.toInt())
                tvTime.setTextColor(0xFFE8EAFF.toInt())
            } else {
                // Other's messages
                frameParams.gravity = Gravity.START
                bubbleContainer.setCardBackgroundColor(0xFFC7D5F5.toInt())
                tvText.setTextColor(0xFF2C3E50.toInt())
                tvTime.setTextColor(0xFF6B7280.toInt())
            }
            
            messageContainer.layoutParams = frameParams
        }
    }
}
