package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.firebase.FirebasePaths
import com.example.finalproject.data.model.User
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

class UsersRepository {

    private val root = FirebaseProvider.databaseRef
    private val usersRef = root.child(FirebasePaths.USERS)
    private val uidToUserRef = root.child(FirebasePaths.UID_TO_USER)


    // finds the next available user number for creating a new user key
    private fun findNextUserNumber(usersSnapshot: DataSnapshot): Int {
        val used = mutableSetOf<Int>()
        for (child in usersSnapshot.children) {
            val key = child.key ?: continue
            if (key.startsWith("user_")) {
                key.removePrefix("user_").toIntOrNull()?.let { used.add(it) }
            }
        }
        var i = 1
        while (used.contains(i)) i++
        return i
    }


    // ensures a user key exists for the given uid or creates a new one
    fun ensureUserKey(
        uid: String,
        onSuccess: (userKey: String) -> Unit,
        onError: (String) -> Unit
    ) {
        uidToUserRef.child(uid).get()
            .addOnSuccessListener { snap ->
                val existing = snap.getValue(String::class.java)
                if (!existing.isNullOrBlank()) {
                    onSuccess(existing)
                    return@addOnSuccessListener
                }

                usersRef.child(uid).get()
                    .addOnSuccessListener { legacySnap ->
                        usersRef.get()
                            .addOnSuccessListener { usersSnap ->
                                val next = findNextUserNumber(usersSnap)
                                val userKey = "user_$next"

                                val updates = hashMapOf<String, Any?>(
                                    "/uid_to_user/$uid" to userKey
                                )

                                if (legacySnap.exists()) {
                                    updates["/users/$userKey"] = legacySnap.value

                                } else {
                                    updates["/users/$userKey/uid"] = uid
                                    updates["/users/$userKey/createdAt"] = ServerValue.TIMESTAMP
                                }

                                root.updateChildren(updates)
                                    .addOnSuccessListener { onSuccess(userKey) }
                                    .addOnFailureListener { e ->
                                        onError(e.message ?: "Failed to create user key")
                                    }
                            }
                            .addOnFailureListener { e ->
                                onError(e.message ?: "Failed to scan users")
                            }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to read legacy user")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to read uid_to_user")
            }
    }


    // creates a new numbered user in firebase with the provided user data
    fun createNumberedUser(
        user: User,
        onSuccess: (userKey: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = user.uid
        if (uid.isBlank()) {
            onError("Missing uid")
            return
        }

        uidToUserRef.child(uid).get()
            .addOnSuccessListener { mapSnap ->
                val existing = mapSnap.getValue(String::class.java)
                if (!existing.isNullOrBlank()) {
                    // Update data there (safe)
                    usersRef.child(existing).setValue(user)
                        .addOnSuccessListener { onSuccess(existing) }
                        .addOnFailureListener { e -> onError(e.message ?: "Failed to update user") }
                    return@addOnSuccessListener
                }

                usersRef.get()
                    .addOnSuccessListener { usersSnap ->
                        val next = findNextUserNumber(usersSnap)
                        val userKey = "user_$next"

                        val updates = hashMapOf<String, Any?>(
                            "/users/$userKey" to user,
                            "/uid_to_user/$uid" to userKey
                        )

                        root.updateChildren(updates)
                            .addOnSuccessListener { onSuccess(userKey) }
                            .addOnFailureListener { e -> onError(e.message ?: "Failed to create user") }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to scan users")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to read mapping")
            }
    }

    // retrieves the nickname for a specific user from firebase
    suspend fun getUserNickname(userKey: String): String? {
        return try {
            usersRef.child(userKey).child(FirebasePaths.NICKNAME)
                .get().await()
                .getValue(String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // checks if a user profile has both username and nickname filled
    suspend fun checkProfileComplete(userKey: String): Boolean {
        return try {
            val snap = usersRef.child(userKey).get().await()
            val username = snap.child(FirebasePaths.USERNAME).getValue(String::class.java).orEmpty()
            val nickname = snap.child(FirebasePaths.NICKNAME).getValue(String::class.java).orEmpty()
            username.isNotBlank() && nickname.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    // suspending version of ensureUserKey for coroutine contexts
    suspend fun ensureUserKeySuspend(uid: String): String {
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            ensureUserKey(
                uid = uid,
                onSuccess = { cont.resume(it, null) },
                onError = { cont.resumeWithException(IllegalStateException(it)) }
            )
        }
    }

    // retrieves a user object from firebase by user key
    suspend fun getUserByKey(userKey: String): User? {
        return try {
            val snap = usersRef.child(userKey).get().await()
            snap.getValue(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

}
