package com.example.finalproject.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object AuthHelper {
    
    // checks if a user profile is complete with username and nickname
    fun checkProfileCompleteness(
        uid: String,
        usersRepo: UsersRepository,
        scope: CoroutineScope,
        onProfileIncomplete: () -> Unit,
        onProfileComplete: () -> Unit,
        onError: () -> Unit
    ) {
        scope.launch {
            try {
                val userKey = usersRepo.ensureUserKeySuspend(uid)
                val isComplete = usersRepo.checkProfileComplete(userKey)
                
                if (isComplete) {
                    onProfileComplete()
                } else {
                    onProfileIncomplete()
                }
            } catch (e: Exception) {
                onError()
            }
        }
    }
}
