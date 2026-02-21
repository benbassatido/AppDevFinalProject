package com.example.finalproject.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.repository.UsersRepository
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.data.repository.AuthHelper
import com.example.finalproject.ui.auth.CompleteProfileFragment
import com.example.finalproject.ui.auth.RegisterFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth = FirebaseProvider.auth
    private lateinit var googleClient: GoogleSignInClient

    private val usersRepo = RepositoryManager.usersRepo

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            val data: Intent? = res.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.result
                val idToken = account.idToken

                if (idToken.isNullOrEmpty()) {
                    ErrorHandler.showError(requireContext(), null, "Google sign-in failed (no token)")
                    return@registerForActivityResult
                }

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        routeAfterLogin()
                    }
                    .addOnFailureListener { e ->
                        ErrorHandler.showError(requireContext(), e.message, "Google login failed")
                    }

            } catch (e: Exception) {
                ErrorHandler.showError(requireContext(), e.message, "Google sign-in failed")
            }
        }

    // initializes the login screen with email and google sign-in options
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivAppLogo = view.findViewById<ImageView>(R.id.ivAppLogo)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnGoogle = view.findViewById<MaterialButton>(R.id.btnGoogle)
        val tvCreateAccount = view.findViewById<TextView>(R.id.tvCreateAccount)

        // Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(requireContext(), gso)

        // Email/Password login
        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString()?.trim().orEmpty()

            etEmail.error = null
            etPassword.error = null

            if (email.isEmpty()) {
                etEmail.error = "Email required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password required"
                return@setOnClickListener
            }

            setLoading(ivAppLogo, btnLogin, btnGoogle, etEmail, etPassword, true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    setLoading(ivAppLogo, btnLogin, btnGoogle, etEmail, etPassword, false)
                    routeAfterLogin()
                }
                .addOnFailureListener { e ->
                    setLoading(ivAppLogo, btnLogin, btnGoogle, etEmail, etPassword, false)
                    ErrorHandler.showError(
                        requireContext(),
                        friendlyAuthError(e.message)
                    )
                }
        }

        // Google login
        btnGoogle.setOnClickListener {
            googleClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleClient.signInIntent)
            }
        }

        // Go to register
        tvCreateAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.authFragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // updates the ui elements to show loading state
    private fun setLoading(
        ivAppLogo: ImageView,
        btnLogin: MaterialButton,
        btnGoogle: MaterialButton,
        etEmail: TextInputEditText,
        etPassword: TextInputEditText,
        loading: Boolean
    ) {
        ivAppLogo.isEnabled = !loading
        btnLogin.isEnabled = !loading
        btnGoogle.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
        btnLogin.text = if (loading) "LOGGING IN..." else "Sign In"
    }

    // routes user to appropriate screen after successful login
    private fun routeAfterLogin() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            goToHome()
            return
        }

        AuthHelper.checkProfileCompleteness(
            uid = uid,
            usersRepo = usersRepo,
            scope = viewLifecycleOwner.lifecycleScope,
            onProfileIncomplete = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.authFragmentContainer, CompleteProfileFragment())
                    .commit()
            },
            onProfileComplete = { goToHome() },
            onError = { goToHome() }
        )
    }


    // navigates to the main activity home screen
    private fun goToHome() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    // converts firebase auth error messages to user friendly text
    private fun friendlyAuthError(msg: String?): String {
        val m = (msg ?: "").lowercase()
        return when {
            "no user record" in m || "user does not exist" in m -> "User doesn't exist"
            "password is invalid" in m || "wrong password" in m -> "Wrong password"
            "badly formatted" in m -> "Email not valid"
            "network error" in m -> "Network error try again later"
            else -> msg ?: "Login failed"
        }
    }
}

