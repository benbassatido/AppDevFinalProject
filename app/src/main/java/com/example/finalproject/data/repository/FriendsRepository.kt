package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendsRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    suspend fun getFriends(): List<Friend> {
        val uid = myUid()

        val snap = db.reference
            .child("users").child(uid)
            .child("friends")
            .get().await()

        val list = mutableListOf<Friend>()
        for (child in snap.children) {
            val friendUid = child.child("uid").getValue(String::class.java) ?: child.key.orEmpty()
            val nickname = child.child("nickname").getValue(String::class.java) ?: ""
            val username = child.child("username").getValue(String::class.java) ?: ""
            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L

            list.add(Friend(friendUid, nickname, username, createdAt))
        }

        return list.sortedBy { it.nickname.lowercase() }
    }

    suspend fun removeFriendMutualAndCleanupRequests(friendUid: String) {
        val me = myUid()
        if (friendUid.isBlank() || friendUid == me) return

        val root = db.reference

        val myFriendsRef = root.child("users").child(me).child("friends")
        val hisFriendsRef = root.child("users").child(friendUid).child("friends")

        removeFriendFromRef(myFriendsRef, friendUid)
        removeFriendFromRef(hisFriendsRef, me)

        val updates = hashMapOf<String, Any?>()
        updates["users/$me/friend_requests_in/$friendUid"] = null
        updates["users/$me/friend_requests_out/$friendUid"] = null
        updates["users/$friendUid/friend_requests_in/$me"] = null
        updates["users/$friendUid/friend_requests_out/$me"] = null

        root.updateChildren(updates).await()
    }

    private suspend fun removeFriendFromRef(friendsRef: DatabaseReference, targetUid: String) {
        friendsRef.child(targetUid).removeValue().await()

        val q = friendsRef.orderByChild("uid").equalTo(targetUid).get().await()
        for (child in q.children) {
            child.ref.removeValue().await()
        }
    }
}
