package com.example.finalproject.ui.friends.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.SearchUserUi

class SearchResultsAdapter(
    private val onAdd: (SearchUserUi, (Boolean) -> Unit) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.VH>() {

    private val items = mutableListOf<SearchUserUi>()

    // updates the adapter with a new list of search results
    fun submit(list: List<SearchUserUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    // creates a new view holder for a search result item in the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false)
        return VH(v)
    }

    // returns the total number of search results in the adapter
    override fun getItemCount() = items.size

    // binds search result data to the view holder at the specified position
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNickname = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
        private val btnAdd = itemView.findViewById<Button>(R.id.btnAddFriend)

        // binds a search user result to the view holder with appropriate button state
        fun bind(user: SearchUserUi) {
            tvNickname.text = user.user.nickname
            tvUsername.text = user.user.username

            btnAdd.setOnClickListener(null)
            btnAdd.visibility = View.VISIBLE

            if (user.isFriend) {
                btnAdd.text = "Friends"
                btnAdd.isClickable = false
                btnAdd.isFocusable = false
                btnAdd.alpha = 1f
                btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_sent)
                btnAdd.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_medium))
                return
            }

            if (user.requestSent) {
                btnAdd.text = "Request sent"
                btnAdd.isClickable = false
                btnAdd.isFocusable = false
                btnAdd.alpha = 1f
                btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_sent)
                btnAdd.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_medium))
                return
            }

            btnAdd.text = "Add friend"
            btnAdd.isClickable = true
            btnAdd.isFocusable = true
            btnAdd.alpha = 1f
            btnAdd.setBackgroundColor(0xFFC8D5E0.toInt())
            btnAdd.setTextColor(0xFF5A6B7C.toInt())

            btnAdd.setOnClickListener {
                btnAdd.text = "Sending..."
                btnAdd.isClickable = false
                btnAdd.isFocusable = false
                btnAdd.alpha = 1f

                onAdd(user) { success ->
                    if (success) {
                        val position = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            items[position] = user.copy(requestSent = true)
                        }

                        btnAdd.text = "Request sent"
                        btnAdd.isClickable = false
                        btnAdd.isFocusable = false
                        btnAdd.background = itemView.context.getDrawable(R.drawable.bg_btn_sent)
                        btnAdd.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_medium))
                    } else {
                        btnAdd.text = "Add friend"
                        btnAdd.isClickable = true
                        btnAdd.isFocusable = true
                        btnAdd.setBackgroundColor(0xFFC8D5E0.toInt())
                        btnAdd.setTextColor(0xFF5A6B7C.toInt())
                    }
                }
            }
        }
    }
}
