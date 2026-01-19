package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendRequestsRepository(
    private val db: FirebaseDatabase = FirebaseProvider.database,
    auth: FirebaseAuth = FirebaseProvider.auth,
    usersRepo: UsersRepository = UsersRepository()
) : BaseRepository(auth, usersRepo) {

    suspend fun getIncomingRequests(): List<User> {
        val meKey = myUserKey()

        val snap = db.reference.child("users").child(meKey)
            .child("friend_requests_in")
            .get().await()

        val list = mutableListOf<User>()
        for (child in snap.children) {
            val otherKey = child.key.orEmpty()
            val nickname = child.child("nickname").getValue(String::class.java) ?: ""
            val username = child.child("username").getValue(String::class.java) ?: ""
            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
            list.add(User(userKey = otherKey, nickname = nickname, username = username, createdAt = createdAt))
        }
        return list.sortedByDescending { it.createdAt }
    }

    suspend fun acceptRequest(req: User) {
        val meKey = myUserKey()
        val otherKey = req.userKey
        if (otherKey.isBlank() || otherKey == meKey) return

        val root = db.reference
        val now = System.currentTimeMillis()

        val meSnap = root.child("users").child(meKey).get().await()
        val myNickname = meSnap.child("nickname").getValue(String::class.java) ?: ""
        val myUsername = meSnap.child("username").getValue(String::class.java) ?: ""

        val otherNickname = req.nickname
        val otherUsername = req.username

        val updates = hashMapOf<String, Any?>()

        // friends both sides
        updates["users/$meKey/friends/$otherKey"] = mapOf(
            "userKey" to otherKey,
            "nickname" to otherNickname,
            "username" to otherUsername,
            "createdAt" to now
        )

        updates["users/$otherKey/friends/$meKey"] = mapOf(
            "userKey" to meKey,
            "nickname" to myNickname,
            "username" to myUsername,
            "createdAt" to now
        )

        // cleanup requests both directions
        updates["users/$meKey/friend_requests_in/$otherKey"] = null
        updates["users/$otherKey/friend_requests_out/$meKey"] = null
        updates["users/$meKey/friend_requests_out/$otherKey"] = null
        updates["users/$otherKey/friend_requests_in/$meKey"] = null

        root.updateChildren(updates).await()
    }

    suspend fun declineRequest(req: User) {
        val meKey = myUserKey()
        val otherKey = req.userKey
        if (otherKey.isBlank() || otherKey == meKey) return

        val root = db.reference
        val updates = hashMapOf<String, Any?>()
        updates["users/$meKey/friend_requests_in/$otherKey"] = null
        updates["users/$otherKey/friend_requests_out/$meKey"] = null
        updates["users/$meKey/friend_requests_out/$otherKey"] = null
        updates["users/$otherKey/friend_requests_in/$meKey"] = null

        root.updateChildren(updates).await()
    }
}
