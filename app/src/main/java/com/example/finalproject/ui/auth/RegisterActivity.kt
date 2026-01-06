package com.example.finalproject.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etNickname = findViewById<EditText>(R.id.etNickname)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()
            val nickname = etNickname.text.toString().trim()

            when {
                email.isEmpty() -> etEmail.error = "Email is required"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> etEmail.error = "Invalid email"
                username.isEmpty() -> etUsername.error = "Username is required"
                password.length < 6 -> etPassword.error = "Password must be at least 6 characters"
                nickname.isEmpty() -> etNickname.error = "Nickname is required"
                else -> {
                    Toast.makeText(this, "Registered (not saved yet)", Toast.LENGTH_SHORT).show()
                    finish() //
                }
            }
        }
    }
}
