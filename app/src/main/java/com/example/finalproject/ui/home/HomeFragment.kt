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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Game
import com.example.finalproject.ui.auth.AuthActivity
import com.example.finalproject.ui.friends.FriendsFragment
import com.example.finalproject.ui.games.GameDetailsFragment
import com.example.finalproject.ui.games.GameVariantFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: GamesAdapter
    private lateinit var allGames: List<Game>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHello = view.findViewById<TextView>(R.id.tvHelloUser)
        val rv = view.findViewById<RecyclerView>(R.id.rvGames)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        val btnViewFriends = view.findViewById<MaterialButton>(R.id.btnViewFriends)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)

        // Auth check
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {

            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
            return
        }

        val uid = currentUser.uid

        // Hello nickname
        tvHello.text = "Hello Player"
        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("nickname")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nickname = snapshot.getValue(String::class.java)
                    tvHello.text = if (!nickname.isNullOrBlank()) "Hello $nickname" else "Hello Player"
                }

                override fun onCancelled(error: DatabaseError) {
                    tvHello.text = "Hello Player"
                    Toast.makeText(requireContext(), "DB read failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })


        rv.layoutManager = GridLayoutManager(requireContext(), 3)

        allGames = listOf(
            Game("fortnite", "Fortnite", R.drawable.logo_fortnite),
            Game("cs2", "CS2", R.drawable.logo_cs2),
            Game("arc_riders", "Arc Riders", R.drawable.logo_arc_riders),
            Game("battlefield_6", "Battlefield 6", R.drawable.logo_battlefield_6),
            Game("cod_bo7", "COD Black Ops 7", R.drawable.logo_cod_black_ops),
            Game("valorant", "Valorant", R.drawable.logo_valorant)
        )

        adapter = GamesAdapter(allGames) { game ->
            if (game.id == "fortnite") {
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
            } else {
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
        rv.adapter = adapter

        // View Friends
        btnViewFriends.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FriendsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Logout
        btnLogout.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(requireContext(), gso).signOut()
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        // Search
        etSearch.doAfterTextChanged { text ->
            val q = text?.toString()?.trim().orEmpty().lowercase()
            val filtered =
                if (q.isEmpty()) allGames
                else allGames.filter { it.name.lowercase().contains(q) }
            adapter.update(filtered)
        }
    }
}
