package com.example.finalproject.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseProvider {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    
    val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    
    val databaseRef: DatabaseReference by lazy { database.reference }
}
