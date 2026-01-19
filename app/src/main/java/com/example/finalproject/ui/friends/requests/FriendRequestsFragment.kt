package com.example.finalproject.ui.friends.requests

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.FriendRequestsRepository
import kotlinx.coroutines.launch

class FriendRequestsFragment : Fragment(R.layout.fragment_friend_requests) {

    private val friendRequestsRepo = FriendRequestsRepository()

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
            val list = friendRequestsRepo.getIncomingRequests()
            adapter.submit(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun handleAccept(req: User) {
        viewLifecycleOwner.lifecycleScope.launch {
            friendRequestsRepo.acceptRequest(req)
            loadRequests()

            parentFragmentManager.setFragmentResult("friends_refresh", Bundle())
        }
    }


    private fun handleDecline(req: User) {
        viewLifecycleOwner.lifecycleScope.launch {
            friendRequestsRepo.declineRequest(req)
            loadRequests()

            parentFragmentManager.setFragmentResult("friends_refresh", Bundle())
        }
    }

}
