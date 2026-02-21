package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.google.firebase.auth.FirebaseAuth

abstract class BaseRepository(
    protected val auth: FirebaseAuth = FirebaseProvider.auth,
    protected val usersRepo: UsersRepository = RepositoryManager.usersRepo
) {
    // returns the current user's uid or throws an error if not logged in
    protected fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    // returns the current user's key from the database
    protected suspend fun myUserKey(): String = usersRepo.ensureUserKeySuspend(myUid())
}
