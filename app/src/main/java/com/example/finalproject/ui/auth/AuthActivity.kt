package com.example.finalproject.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finalproject.MainActivity
import com.example.finalproject.R
import com.example.finalproject.ui.login.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val uid = user.uid

            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(uid)
                .get()
                .addOnSuccessListener { snap ->
                    val username = snap.child("username").getValue(String::class.java).orEmpty()
                    val nickname = snap.child("nickname").getValue(String::class.java).orEmpty()

                    if (username.isBlank() || nickname.isBlank()) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.authFragmentContainer, CompleteProfileFragment())
                            .commit()
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.authFragmentContainer, LoginFragment())
                .commit()
        }
    }
}
