package com.example.finalproject.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.R
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.auth.AuthActivity
import com.example.finalproject.ui.common.ErrorHandler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val auth = FirebaseProvider.auth
    private val usersRepo = RepositoryManager.usersRepo

    // initializes profile screen and loads user information
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvNickname = view.findViewById<TextView>(R.id.tvProfileNickname)
        val tvUsername = view.findViewById<TextView>(R.id.tvProfileUsername)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<MaterialCardView>(R.id.btnLogoutProfile)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
            return
        }

        val uid = currentUser.uid
        tvEmail.text = currentUser.email ?: "No email"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userKey = usersRepo.ensureUserKeySuspend(uid)
                val user = usersRepo.getUserByKey(userKey)
                
                if (user != null) {
                    tvNickname.text = user.nickname.ifBlank { "No nickname" }
                    tvUsername.text = "@${user.username.ifBlank { "user" }}"
                } else {
                    tvNickname.text = "Player"
                    tvUsername.text = "@user"
                }
            } catch (e: Exception) {
                tvNickname.text = "Player"
                tvUsername.text = "@user"
                ErrorHandler.showError(requireContext(), e.message, "Failed to load profile")
            }
        }

        btnLogout.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(requireContext(), gso).signOut()
            auth.signOut()

            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }
}
