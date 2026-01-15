package com.example.finalproject.ui.friends.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.SearchUser

class SearchResultsAdapter(
    private val onAdd: (SearchUser, (Boolean) -> Unit) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.VH>() {

    private val items = mutableListOf<SearchUser>()

    fun submit(list: List<SearchUser>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNickname = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        private val btnAdd = itemView.findViewById<Button>(R.id.btnAddFriend)

        fun bind(user: SearchUser) {
            tvNickname.text = user.nickname
            tvUsername.text = user.username

            btnAdd.setOnClickListener(null)
            btnAdd.visibility = View.VISIBLE

            if (user.isFriend) {
                btnAdd.text = "Friends"
                btnAdd.isClickable = false
                btnAdd.isFocusable = false
                btnAdd.alpha = 1f
                btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_sent)
                btnAdd.setTextColor(0xFFB5BAC1.toInt())
                return
            }

            if (user.requestSent) {
                btnAdd.text = "Request sent"
                btnAdd.isClickable = false
                btnAdd.isFocusable = false
                btnAdd.alpha = 1f
                btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_sent)
                btnAdd.setTextColor(0xFFB5BAC1.toInt())
                return
            }

            btnAdd.text = "Add friend"
            btnAdd.isClickable = true
            btnAdd.isFocusable = true
            btnAdd.alpha = 1f
            btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_add)
            btnAdd.setTextColor(0xFFFFFFFF.toInt())

            btnAdd.setOnClickListener {
                btnAdd.text = "Sending..."
                btnAdd.isClickable = false
                btnAdd.isFocusable = false
                btnAdd.alpha = 1f

                onAdd(user) { success ->
                    if (success) {

                        user.requestSent = true

                        btnAdd.text = "Request sent"
                        btnAdd.isClickable = false
                        btnAdd.isFocusable = false
                        btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_sent)
                        btnAdd.setTextColor(0xFFB5BAC1.toInt())
                    } else {
                        btnAdd.text = "Add friend"
                        btnAdd.isClickable = true
                        btnAdd.isFocusable = true
                        btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_add)
                        btnAdd.setTextColor(0xFFFFFFFF.toInt())
                    }
                }
            }
        }
    }
}
