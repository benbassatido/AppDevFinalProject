package com.example.finalproject.ui.friends

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.repository.FriendsRepository
import com.example.finalproject.data.repository.FriendsSearchRepository
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

class FriendsFragment : Fragment(R.layout.fragment_friends) {

    private val friendsRepo = FriendsRepository()
    private val searchRepo = FriendsSearchRepository()

    // Search views
    private lateinit var tilSearchFriends: TextInputLayout
    private lateinit var etSearchFriends: TextInputEditText
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var tvNoResults: TextView
    private lateinit var searchAdapter: SearchResultsAdapter
    private var searchJob: Job? = null
    private var isSearchOpen = false

    // Top / tabs
    private lateinit var btnBack: ImageButton
    private lateinit var btnTabFriends: Button
    private lateinit var btnTabRequests: Button
    private lateinit var tvSection: TextView
    private lateinit var tvTitle: TextView

    // Friends list
    private lateinit var rvFriends: RecyclerView
    private lateinit var tvEmptyFriends: TextView
    private lateinit var requestsContainer: View
    private lateinit var btnFindTeammates: Button
    private lateinit var friendsAdapter: FriendsAdapter

    private val friendsUids = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        btnBack = view.findViewById(R.id.btnBack)
        btnTabFriends = view.findViewById(R.id.btnTabFriends)
        btnTabRequests = view.findViewById(R.id.btnTabRequests)
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

        // Title gradient
        tvTitle.post {
            val paint = tvTitle.paint
            val width = paint.measureText(tvTitle.text.toString())
            paint.shader = LinearGradient(
                0f, 0f, width, tvTitle.textSize,
                intArrayOf(0xFF39C6FF.toInt(), 0xFF7C3AED.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            tvTitle.invalidate()
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Tabs
        btnTabFriends.setOnClickListener {
            closeSearchIfOpen()
            showFriendsTab()
        }
        btnTabRequests.setOnClickListener {
            closeSearchIfOpen()
            showRequestsTab()
        }

        childFragmentManager.setFragmentResultListener("friends_refresh", viewLifecycleOwner) { _, _ ->
            loadFriends()
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
                        // UI immediate
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
                    if (friendsUids.contains(user.uid)) {
                        done(false)
                        return@launch
                    }
                    searchRepo.sendFriendRequest(user.uid)
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
                            u.isFriend = friendsUids.contains(u.uid)
                            u.requestSent = outgoingSet.contains(u.uid)
                            u
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

        // Default
        showFriendsTab()
        hideSearchViews()
        loadFriends()
    }

    private fun showFriendsTab() {
        rvFriends.visibility = View.VISIBLE
        requestsContainer.visibility = View.GONE

        btnTabFriends.setBackgroundResource(R.drawable.bg_tab_selected)
        btnTabFriends.setTextColor(0xFF39C6FF.toInt())

        btnTabRequests.setBackgroundResource(R.drawable.bg_tab_unselected)
        btnTabRequests.setTextColor(0xFFFFFFFF.toInt())

        loadFriends()
    }

    private fun showRequestsTab() {
        rvFriends.visibility = View.GONE
        tvEmptyFriends.visibility = View.GONE
        requestsContainer.visibility = View.VISIBLE

        btnTabRequests.setBackgroundResource(R.drawable.bg_tab_selected)
        btnTabRequests.setTextColor(0xFF39C6FF.toInt())

        btnTabFriends.setBackgroundResource(R.drawable.bg_tab_unselected)
        btnTabFriends.setTextColor(0xFFFFFFFF.toInt())

        val existing = childFragmentManager.findFragmentById(R.id.requestsContainer)
        if (existing !is FriendRequestsFragment) {
            childFragmentManager.beginTransaction()
                .replace(R.id.requestsContainer, FriendRequestsFragment())
                .commit()
        }
    }


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


    private fun closeSearchIfOpen() {
        if (!isSearchOpen) return
        isSearchOpen = false
        hideSearchViews()
        tvSection.text = "Friends List"
    }

    private fun hideSearchViews() {
        etSearchFriends.setText("")
        tilSearchFriends.visibility = View.GONE
        rvSearchResults.visibility = View.GONE
        tvNoResults.visibility = View.GONE
        searchAdapter.submit(emptyList())
    }


    private fun loadFriends() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list = friendsRepo.getFriends()
                friendsAdapter.submit(list)

                friendsUids.clear()
                friendsUids.addAll(list.map { it.userKey })

                tvEmptyFriends.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            } catch (_: Exception) {
            }
        }
    }
}
