package com.example.finalproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.UsersRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.ServerValue
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler

class CompleteProfileFragment : Fragment(R.layout.fragment_complete_profile) {

    private val auth = FirebaseProvider.auth
    private val db = FirebaseProvider.databaseRef
    private val usersRepo = RepositoryManager.usersRepo

    // initializes profile completion form and handles back press
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsername = view.findViewById<TextInputEditText>(R.id.etUsername)
        val etNickname = view.findViewById<TextInputEditText>(R.id.etNickname)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveProfile)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(requireContext(), "Please complete your profile first", Toast.LENGTH_SHORT).show()
                }
            }
        )

        btnSave.setOnClickListener {
            val user = auth.currentUser
            val uid = user?.uid.orEmpty()
            val email = user?.email.orEmpty()

            if (uid.isBlank()) {
                Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val username = etUsername.text?.toString()?.trim().orEmpty()
            val nickname = etNickname.text?.toString()?.trim().orEmpty()

            etUsername.error = null
            etNickname.error = null

            if (username.length < 3) { etUsername.error = "Username must be at least 3 characters"; return@setOnClickListener }
            if (nickname.length < 2) { etNickname.error = "Nickname must be at least 2 characters"; return@setOnClickListener }

            val validUsername = Regex("^[a-zA-Z0-9_]+$")
            if (!validUsername.matches(username)) {
                etUsername.error = "Only letters, numbers and _"
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "SAVING..."

            usersRepo.ensureUserKey(
                uid = uid,
                onSuccess = { userKey ->

                    db.child("users")
                        .orderByChild("username")
                        .equalTo(username)
                        .get()
                        .addOnSuccessListener { snap ->
                            val takenByOther = snap.children.any { child ->
                                val otherUid = child.child("uid").getValue(String::class.java).orEmpty()
                                otherUid.isNotBlank() && otherUid != uid
                            }

                            if (takenByOther) {
                                btnSave.isEnabled = true
                                btnSave.text = "SAVE PROFILE"
                                etUsername.error = "Username already taken"
                                return@addOnSuccessListener
                            }

                            val userObj = User(
                                uid = uid,
                                email = email,
                                username = username,
                                nickname = nickname,
                                nicknameLower = nickname.lowercase(),
                                createdAt = 0L
                            )

                            val updates = hashMapOf<String, Any?>(
                                "/users/$userKey/uid" to userObj.uid,
                                "/users/$userKey/email" to userObj.email,
                                "/users/$userKey/username" to userObj.username,
                                "/users/$userKey/nickname" to userObj.nickname,
                                "/users/$userKey/nicknameLower" to userObj.nicknameLower,
                                "/users/$userKey/createdAt" to ServerValue.TIMESTAMP
                            )

                            db.updateChildren(updates)
                                .addOnSuccessListener {
                                    val intent = Intent(requireContext(), MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    btnSave.isEnabled = true
                                    btnSave.text = "SAVE PROFILE"
                                    Toast.makeText(requireContext(), e.message ?: "Save failed", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            btnSave.isEnabled = true
                            btnSave.text = "SAVE PROFILE"
                            Toast.makeText(requireContext(), e.message ?: "Failed to validate username", Toast.LENGTH_LONG).show()
                        }
                },
                onError = { msg ->
                    btnSave.isEnabled = true
                    btnSave.text = "SAVE PROFILE"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
