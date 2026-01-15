package com.example.finalproject.ui.friends.requests

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.FriendRequest
import com.example.finalproject.data.repository.FriendRequestsRepository
import kotlinx.coroutines.launch

class FriendRequestsFragment : Fragment(R.layout.fragment_friend_requests) {

    private val repo = FriendRequestsRepository()

    private lateinit var rvRequests: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: FriendRequestsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvRequests = view.findViewById(R.id.rvRequests)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        adapter = FriendRequestsAdapter(
            onAccept = { req -> handleAccept(req) },
            onDecline = { req -> handleDecline(req) }
        )

        rvRequests.layoutManager = LinearLayoutManager(requireContext())
        rvRequests.adapter = adapter

        loadRequests()
    }

    private fun loadRequests() {
        viewLifecycleOwner.lifecycleScope.launch {
            val list = repo.getIncomingRequests()
            adapter.submit(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun handleAccept(req: FriendRequest) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.acceptRequest(req)
            loadRequests()

            parentFragmentManager.setFragmentResult("friends_refresh", Bundle())
        }
    }


    private fun handleDecline(req: FriendRequest) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.declineRequest(req)
            loadRequests()

            parentFragmentManager.setFragmentResult("friends_refresh", Bundle())
        }
    }

}
