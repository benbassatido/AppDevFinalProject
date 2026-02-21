package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.ui.auth.AuthActivity
import com.example.finalproject.ui.home.HomeFragment
import com.example.finalproject.ui.friends.FriendsFragment
import com.example.finalproject.ui.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseProvider.auth
    private lateinit var bottomNavigation: BottomNavigationView

    // initializes the main activity and sets up navigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(sys.left, sys.top, sys.right, maxOf(sys.bottom, ime.bottom))
            insets
        }

        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_friends -> {
                    loadFragment(FriendsFragment())
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.navigation_home
        }

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // First check if there are fragments in the back stack (sub-pages)
                if (supportFragmentManager.backStackEntryCount > 0) {
                    // Pop the back stack (go back to previous fragment)
                    supportFragmentManager.popBackStack()
                } else {
                    // No back stack, handle tab navigation
                    when (bottomNavigation.selectedItemId) {
                        R.id.navigation_home -> {
                            // If on home tab, exit app
                            finish()
                        }
                        else -> {
                            // If on any other tab, go back to home
                            bottomNavigation.selectedItemId = R.id.navigation_home
                        }
                    }
                }
            }
        })
    }

    // loads a fragment into the fragment container
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
