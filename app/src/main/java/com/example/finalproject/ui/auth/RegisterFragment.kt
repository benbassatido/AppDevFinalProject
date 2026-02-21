package com.example.finalproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.UsersRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val auth = FirebaseProvider.auth
    private val usersRepo = RepositoryManager.usersRepo

    // initializes registration form and sets up validation logic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etUsername = view.findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val etNickname = view.findViewById<TextInputEditText>(R.id.etNickname)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)
        val btnTogglePassword = view.findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnToggleConfirmPassword = view.findViewById<ImageButton>(R.id.btnToggleConfirmPassword)

        // Password toggle functionality
        var isPasswordVisible = false
        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }

        // Confirm password toggle functionality
        var isConfirmPasswordVisible = false
        btnToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etConfirmPassword.setSelection(etConfirmPassword.text?.length ?: 0)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val username = etUsername.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            val confirmPassword = etConfirmPassword.text?.toString().orEmpty()
            val nickname = etNickname.text?.toString()?.trim().orEmpty()

            etEmail.error = null
            etUsername.error = null
            etPassword.error = null
            etConfirmPassword.error = null
            etNickname.error = null

            when {
                email.isEmpty() -> { etEmail.error = "Email is required"; return@setOnClickListener }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { etEmail.error = "Invalid email"; return@setOnClickListener }
                username.isEmpty() -> { etUsername.error = "Username is required"; return@setOnClickListener }
                password.length < 6 -> { etPassword.error = "Password must be at least 6 characters"; return@setOnClickListener }
                confirmPassword.isEmpty() -> { etConfirmPassword.error = "Please confirm your password"; return@setOnClickListener }
                password != confirmPassword -> { etConfirmPassword.error = "Passwords do not match"; return@setOnClickListener }
                nickname.isEmpty() -> { etNickname.error = "Nickname is required"; return@setOnClickListener }
            }

            setLoading(btnRegister, true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    if (firebaseUser == null) {
                        setLoading(btnRegister, false)
                        Toast.makeText(requireContext(), "Register failed", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val uid = firebaseUser.uid

                    val userObj = User(
                        uid = uid,
                        email = email,
                        username = username,
                        nickname = nickname,
                        nicknameLower = nickname.lowercase(),
                        createdAt = System.currentTimeMillis()
                    )

                    usersRepo.createNumberedUser(
                        user = userObj,
                        onSuccess = {
                            setLoading(btnRegister, false)
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        },
                        onError = { msg ->
                            setLoading(btnRegister, false)
                            Toast.makeText(requireContext(), "DB FAIL: $msg", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                .addOnFailureListener { e ->
                    setLoading(btnRegister, false)
                    Toast.makeText(requireContext(), "Auth FAIL: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // updates the button state to show loading status
    private fun setLoading(btn: MaterialButton, loading: Boolean) {
        btn.isEnabled = !loading
        btn.alpha = if (loading) 0.6f else 1.0f
        btn.text = if (loading) "CREATING..." else "Create Account"
    }
}


