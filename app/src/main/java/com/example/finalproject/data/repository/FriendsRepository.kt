package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendsRepository(
    private val db: FirebaseDatabase = FirebaseProvider.database,
    auth: FirebaseAuth = FirebaseProvider.auth,
    usersRepo: UsersRepository = UsersRepository()
) : BaseRepository(auth, usersRepo) {

    suspend fun getFriends(): List<User> {
        val meKey = myUserKey()

        val snap = db.reference
            .child("users").child(meKey)
            .child("friends")
            .get().await()

        val list = mutableListOf<User>()
        for (child in snap.children) {
            val friendKey = child.key.orEmpty()
            val nickname = child.child("nickname").getValue(String::class.java) ?: ""
            val username = child.child("username").getValue(String::class.java) ?: ""
            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L

            list.add(User(userKey = friendKey, nickname = nickname, username = username, createdAt = createdAt))
        }

        return list.sortedBy { it.nickname.lowercase() }
    }

    suspend fun removeFriendMutualAndCleanupRequests(friendUserKey: String) {
        val meKey = myUserKey()
        if (friendUserKey.isBlank() || friendUserKey == meKey) return

        val root = db.reference

        val myFriendsRef = root.child("users").child(meKey).child("friends")
        val hisFriendsRef = root.child("users").child(friendUserKey).child("friends")

        removeFriendFromRef(myFriendsRef, friendUserKey)
        removeFriendFromRef(hisFriendsRef, meKey)

        val updates = hashMapOf<String, Any?>()
        updates["users/$meKey/friend_requests_in/$friendUserKey"] = null
        updates["users/$meKey/friend_requests_out/$friendUserKey"] = null
        updates["users/$friendUserKey/friend_requests_in/$meKey"] = null
        updates["users/$friendUserKey/friend_requests_out/$meKey"] = null

        root.updateChildren(updates).await()
    }

    private suspend fun removeFriendFromRef(friendsRef: DatabaseReference, targetKey: String) {
        friendsRef.child(targetKey).removeValue().await()
    }
}
