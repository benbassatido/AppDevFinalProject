package com.example.finalproject.data.repository

import com.example.finalproject.data.model.AppUser
import com.google.firebase.database.*

class UsersRepository {

    private val root = FirebaseDatabase.getInstance().reference
    private val usersRef = root.child("users")
    private val uidToUserRef = root.child("uid_to_user")


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


    fun getUserKeyByUid(
        uid: String,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        uidToUserRef.child(uid).get()
            .addOnSuccessListener { snap ->
                onSuccess(snap.getValue(String::class.java))
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to read uid_to_user")
            }
    }


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


    fun createNumberedUser(
        user: AppUser,
        onSuccess: (userKey: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = user.uid
        if (uid.isBlank()) {
            onError("Missing uid")
            return
        }

        // If mapping exists, don't create again
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

                // Create new slot user_X
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


    fun readUserByUid(
        uid: String,
        onSuccess: (AppUser?) -> Unit,
        onError: (String) -> Unit
    ) {
        ensureUserKey(
            uid = uid,
            onSuccess = { userKey ->
                usersRef.child(userKey).get()
                    .addOnSuccessListener { snap ->
                        onSuccess(snap.getValue(AppUser::class.java))
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to read user")
                    }
            },
            onError = onError
        )
    }
}
