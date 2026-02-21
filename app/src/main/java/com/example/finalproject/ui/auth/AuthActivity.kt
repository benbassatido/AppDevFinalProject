package com.example.finalproject.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.repository.AuthHelper
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.login.LoginFragment

class AuthActivity : AppCompatActivity() {

    private val auth = FirebaseProvider.auth
    private val usersRepo = RepositoryManager.usersRepo

    // initializes the activity and checks if user profile is complete
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                sys.left,
                sys.top,
                sys.right,
                maxOf(sys.bottom, ime.bottom)
            )
            insets
        }

        val user = auth.currentUser

        if (user != null) {
            val uid = user.uid

            AuthHelper.checkProfileCompleteness(
                uid = uid,
                usersRepo = usersRepo,
                scope = lifecycleScope,
                onProfileIncomplete = {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.authFragmentContainer, CompleteProfileFragment())
                        .commit()
                },
                onProfileComplete = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onError = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.authFragmentContainer, LoginFragment())
                .commit()
        }
    }
}
