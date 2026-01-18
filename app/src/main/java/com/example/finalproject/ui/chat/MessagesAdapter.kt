package com.example.finalproject.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(
    private val myUserKey: String,
    private val otherNickname: String
) : RecyclerView.Adapter<MessagesAdapter.VH>() {

    private val items = mutableListOf<ChatMessage>()
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun submit(list: List<ChatMessage>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val prev = if (position > 0) items[position - 1] else null
        holder.bind(items[position], prev, myUserKey, otherNickname, timeFmt)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bubbleContainer = itemView.findViewById<LinearLayout>(R.id.bubbleContainer)
        private val tvSender = itemView.findViewById<TextView>(R.id.tvSender)
        private val tvText = itemView.findViewById<TextView>(R.id.tvText)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        fun bind(
            msg: ChatMessage,
            prev: ChatMessage?,
            myUserKey: String,
            otherNickname: String,
            timeFmt: SimpleDateFormat
        ) {
            val isMine = msg.senderId == myUserKey
            val prevSameSender = prev?.senderId == msg.senderId

            if (!prevSameSender) {
                tvSender.visibility = View.VISIBLE
                tvSender.text = if (isMine) "You" else otherNickname
            } else {
                tvSender.visibility = View.GONE
            }

            tvText.text = msg.text
            tvTime.text = timeFmt.format(Date(msg.createdAt))

            val frameParams = bubbleContainer.layoutParams as FrameLayout.LayoutParams
            if (isMine) {
                bubbleContainer.setBackgroundResource(R.drawable.bg_bubble_me_soft)
                frameParams.gravity = Gravity.END
            } else {
                bubbleContainer.setBackgroundResource(R.drawable.bg_bubble_other_soft)
                frameParams.gravity = Gravity.START
            }
            bubbleContainer.layoutParams = frameParams
        }
    }
}
