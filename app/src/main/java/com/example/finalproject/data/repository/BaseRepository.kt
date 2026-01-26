package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.google.firebase.auth.FirebaseAuth

abstract class BaseRepository(
    protected val auth: FirebaseAuth = FirebaseProvider.auth,
    protected val usersRepo: UsersRepository = RepositoryManager.usersRepo
) {
    protected fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    protected suspend fun myUserKey(): String = usersRepo.ensureUserKeySuspend(myUid())
}
