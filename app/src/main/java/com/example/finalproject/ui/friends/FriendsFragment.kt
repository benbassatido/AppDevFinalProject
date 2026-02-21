package com.example.finalproject.ui.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.ui.chat.ChatFragment
import com.example.finalproject.ui.friends.adapters.FriendsAdapter
import com.example.finalproject.ui.friends.adapters.SearchResultsAdapter
import com.example.finalproject.ui.friends.requests.FriendRequestsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler


class FriendsFragment : Fragment(R.layout.fragment_friends) {

    private val friendsRepo = RepositoryManager.friendsRepo
    private val searchRepo = RepositoryManager.friendsSearchRepo

    // Search views
    private lateinit var tilSearchFriends: TextInputLayout
    private lateinit var etSearchFriends: TextInputEditText
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var tvNoResults: TextView
    private lateinit var searchAdapter: SearchResultsAdapter
    private var searchJob: Job? = null
    private var isSearchOpen = false

    private lateinit var tabFriends: LinearLayout
    private lateinit var tabRequests: LinearLayout
    private lateinit var btnTabFriends: TextView
    private lateinit var btnTabRequests: TextView
    private lateinit var underlineFriends: View
    private lateinit var underlineRequests: View
    private lateinit var tvSection: TextView
    private lateinit var tvTitle: TextView

    private lateinit var rvFriends: RecyclerView
    private lateinit var tvEmptyFriends: TextView
    private lateinit var requestsContainer: View
    private lateinit var btnFindTeammates: Button
    private lateinit var friendsAdapter: FriendsAdapter

    private val friendsUids = mutableListOf<String>()

    // initializes the friends screen with tabs and search functionality
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        tabFriends = view.findViewById(R.id.tabFriends)
        tabRequests = view.findViewById(R.id.tabRequests)
        btnTabFriends = view.findViewById(R.id.btnTabFriends)
        btnTabRequests = view.findViewById(R.id.btnTabRequests)
        underlineFriends = view.findViewById(R.id.underlineFriends)
        underlineRequests = view.findViewById(R.id.underlineRequests)
        tvSection = view.findViewById(R.id.tvSection)
        tvTitle = view.findViewById(R.id.tvTitle)

        rvFriends = view.findViewById(R.id.rvFriends)
        tvEmptyFriends = view.findViewById(R.id.tvEmptyFriends)
        requestsContainer = view.findViewById(R.id.requestsContainer)
        btnFindTeammates = view.findViewById(R.id.btnFindTeammates)

        tilSearchFriends = view.findViewById(R.id.tilSearchFriends)
        etSearchFriends = view.findViewById(R.id.etSearchFriends)
        rvSearchResults = view.findViewById(R.id.rvSearchResults)
        tvNoResults = view.findViewById(R.id.tvNoResults)

        tabFriends.setOnClickListener {
            closeSearchIfOpen()
            showFriendsTab()
        }
        tabRequests.setOnClickListener {
            closeSearchIfOpen()
            showRequestsTab()
        }

        childFragmentManager.setFragmentResultListener("friends_refresh", viewLifecycleOwner) { _, _ ->
            loadFriends()
            // Also refresh search results if search is open
            viewLifecycleOwner.lifecycleScope.launch {
                delay(300)
                refreshSearchResults()
            }
        }

        // Friends adapter
        friendsAdapter = FriendsAdapter(
            onFriendClick = { friend ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ChatFragment.newInstance(friend.userKey, friend.nickname))
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { friend ->
                MaterialAlertDialogBuilder(requireContext(), R.style.DarkConfirmDialog)
                    .setMessage("Are you sure you want to delete this friend?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        friendsAdapter.removeByUid(friend.userKey)
                        friendsUids.remove(friend.userKey)
                        tvEmptyFriends.visibility =
                            if (friendsAdapter.currentList().isEmpty()) View.VISIBLE else View.GONE

                        // DB delete
                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                friendsRepo.removeFriendMutualAndCleanupRequests(friend.userKey)
                            } catch (_: Exception) {
                                loadFriends()
                            }
                        }
                    }
                    .show()
            }
        )
        rvFriends.layoutManager = LinearLayoutManager(requireContext())
        rvFriends.adapter = friendsAdapter

        // Search adapter
        searchAdapter = SearchResultsAdapter { user, done ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (friendsUids.contains(user.user.userKey)) {
                        done(false)
                        return@launch
                    }
                    searchRepo.sendFriendRequest(user.user.userKey)
                    
                    // Reload friends list to check if friendship was established (auto-accept case)
                    loadFriends()
                    
                    // Wait a bit for the load to complete and then refresh search results
                    delay(500)
                    refreshSearchResults()
                    
                    done(true)
                } catch (_: Exception) {
                    done(false)
                }
            }
        }
        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.adapter = searchAdapter

        // Search input listener
        etSearchFriends.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString().orEmpty()

                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)

                    if (!isSearchOpen) return@launch

                    if (q.trim().length < 2) {
                        rvSearchResults.visibility = View.GONE
                        tvNoResults.visibility = View.GONE
                        searchAdapter.submit(emptyList())
                        return@launch
                    }

                    try {
                        val results = searchRepo.searchByNicknamePrefix(q)

                        val outgoingSet = searchRepo.getMyOutgoingRequestsSet()

                        val fixed = results.map { u ->
                            u.copy(
                                isFriend = friendsUids.contains(u.user.userKey),
                                requestSent = outgoingSet.contains(u.user.userKey)
                            )
                        }

                        rvSearchResults.visibility = View.VISIBLE
                        if (fixed.isEmpty()) {
                            searchAdapter.submit(emptyList())
                            tvNoResults.visibility = View.VISIBLE
                        } else {
                            searchAdapter.submit(fixed)
                            tvNoResults.visibility = View.GONE
                        }
                    } catch (_: Exception) {
                    }

                }
            }
        })

        btnFindTeammates.setOnClickListener { toggleSearch() }

        showFriendsTab()
        hideSearchViews()
        loadFriends()
    }

    // switches to the friends tab view
    private fun showFriendsTab() {
        rvFriends.visibility = View.VISIBLE
        requestsContainer.visibility = View.GONE

        btnTabFriends.setTextColor(0xFF8BA7F5.toInt())
        underlineFriends.setBackgroundColor(0xFF8BA7F5.toInt())

        btnTabRequests.setTextColor(0xFFC0C0C0.toInt())
        underlineRequests.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        loadFriends()
    }

    // switches to the friend requests tab view
    private fun showRequestsTab() {
        rvFriends.visibility = View.GONE
        tvEmptyFriends.visibility = View.GONE
        requestsContainer.visibility = View.VISIBLE

        btnTabRequests.setTextColor(0xFF8BA7F5.toInt())
        underlineRequests.setBackgroundColor(0xFF8BA7F5.toInt())

        btnTabFriends.setTextColor(0xFFC0C0C0.toInt())
        underlineFriends.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val existing = childFragmentManager.findFragmentById(R.id.requestsContainer)
        if (existing !is FriendRequestsFragment) {
            childFragmentManager.beginTransaction()
                .replace(R.id.requestsContainer, FriendRequestsFragment())
                .commit()
        }
    }


    // toggles the friend search interface visibility
    private fun toggleSearch() {
        showFriendsTab()

        isSearchOpen = !isSearchOpen
        if (isSearchOpen) {
            tilSearchFriends.visibility = View.VISIBLE
            etSearchFriends.requestFocus()

            rvSearchResults.visibility = View.GONE
            tvNoResults.visibility = View.GONE
            searchAdapter.submit(emptyList())

            tvSection.text = "Find New Teammates"
        } else {
            closeSearchIfOpen()
        }
    }


    // closes the search interface if it is currently open
    private fun closeSearchIfOpen() {
        if (!isSearchOpen) return
        isSearchOpen = false
        hideSearchViews()
        tvSection.text = "Friends List"
    }

    // hides all search related views and clears search input
    private fun hideSearchViews() {
        etSearchFriends.setText("")
        tilSearchFriends.visibility = View.GONE
        rvSearchResults.visibility = View.GONE
        tvNoResults.visibility = View.GONE
        searchAdapter.submit(emptyList())
    }


    // loads the list of friends from the repository
    private fun loadFriends() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list = friendsRepo.getFriends()
                friendsAdapter.submit(list)

                friendsUids.clear()
                friendsUids.addAll(list.map { it.userKey })

                tvEmptyFriends.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                ErrorHandler.showError(requireContext(), e.message, "Failed to load friends")
            }
        }
    }

    // refreshes the search results with updated friend status
    private fun refreshSearchResults() {
        val currentQuery = etSearchFriends.text?.toString().orEmpty()
        if (!isSearchOpen || currentQuery.trim().length < 2) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = searchRepo.searchByNicknamePrefix(currentQuery)
                val outgoingSet = searchRepo.getMyOutgoingRequestsSet()

                val fixed = results.map { u ->
                    u.copy(
                        isFriend = friendsUids.contains(u.user.userKey),
                        requestSent = outgoingSet.contains(u.user.userKey)
                    )
                }

                searchAdapter.submit(fixed)
            } catch (_: Exception) {
            }
        }
    }
}
