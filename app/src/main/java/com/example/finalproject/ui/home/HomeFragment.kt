package com.example.finalproject.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import kotlinx.coroutines.launch
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.repository.GamesRepository
import com.example.finalproject.data.repository.UsersRepository
import com.example.finalproject.ui.auth.AuthActivity
import com.example.finalproject.ui.friends.FriendsFragment
import com.example.finalproject.ui.games.GameDetailsFragment
import com.example.finalproject.ui.games.GameVariantFragment
import com.example.finalproject.ui.rooms.RoomsFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: GamesAdapter
    private lateinit var allGames: List<com.example.finalproject.data.model.Game>

    private val auth = FirebaseProvider.auth
    private val usersRepo = UsersRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHello = view.findViewById<TextView>(R.id.tvHelloUser)
        val rv = view.findViewById<RecyclerView>(R.id.rvGames)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        val btnViewFriends = view.findViewById<MaterialButton>(R.id.btnViewFriends)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
            return
        }

        val uid = currentUser.uid

        tvHello.text = "Hello Player"
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userKey = usersRepo.ensureUserKeySuspend(uid)
                val nickname = usersRepo.getUserNickname(userKey)
                tvHello.text = if (!nickname.isNullOrBlank()) "Hello $nickname" else "Hello Player"
            } catch (e: Exception) {
                tvHello.text = "Hello Player"
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 3)

        allGames = GamesRepository.getAllGames()

        adapter = GamesAdapter(allGames) { game ->
            when (game.id) {
                "valorant" -> {
                    val f = GameVariantFragment().apply {
                        arguments = bundleOf(
                            "gameId" to game.id,
                            "gameName" to game.name,
                            "gameLogoRes" to game.logoRes,
                            "directToRooms" to true,
                            "fixedPartyType" to "Competitive",
                            "fixedMaxPlayers" to 5
                        )
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, f)
                        .addToBackStack(null)
                        .commit()
                }
                "fortnite", "cs2", "arc_riders", "battlefield_6", "cod_bo7" -> {
                    val f = GameVariantFragment().apply {
                        arguments = bundleOf(
                            "gameId" to game.id,
                            "gameName" to game.name,
                            "gameLogoRes" to game.logoRes
                        )
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, f)
                        .addToBackStack(null)
                        .commit()
                }
                else -> {
                    val f = GameDetailsFragment().apply {
                        arguments = bundleOf(
                            "gameId" to game.id,
                            "gameName" to game.name,
                            "gameLogoRes" to game.logoRes,
                            "variantId" to "",
                            "variantTitle" to ""
                        )
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, f)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        rv.adapter = adapter

        btnViewFriends.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FriendsFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogout.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(requireContext(), gso).signOut()
            auth.signOut()

            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        etSearch.doAfterTextChanged { text ->
            val q = text?.toString()?.trim().orEmpty()
            val filtered = GamesRepository.searchGames(q)
            adapter.update(filtered)
        }
    }
}
