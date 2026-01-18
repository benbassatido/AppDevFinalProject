package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendsRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val usersRepo: UsersRepository = UsersRepository()
) {
    private fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    private suspend fun myUserKey(): String = usersRepo.ensureUserKeySuspend(myUid())

    suspend fun getFriends(): List<Friend> {
        val meKey = myUserKey()

        val snap = db.reference
            .child("users").child(meKey)
            .child("friends")
            .get().await()

        val list = mutableListOf<Friend>()
        for (child in snap.children) {
            val friendKey = child.key.orEmpty()
            val nickname = child.child("nickname").getValue(String::class.java) ?: ""
            val username = child.child("username").getValue(String::class.java) ?: ""
            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L

            list.add(Friend(userKey = friendKey, nickname = nickname, username = username, createdAt = createdAt))
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

        val q = friendsRef.orderByChild("userKey").equalTo(targetKey).get().await()
        for (child in q.children) {
            child.ref.removeValue().await()
        }
    }
}
