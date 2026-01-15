package com.example.finalproject.data.repository

import com.example.finalproject.data.model.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendRequestsRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    suspend fun getIncomingRequests(): List<FriendRequest> {
        val me = myUid()

        val snap = db.reference.child("users").child(me)
            .child("friend_requests_in")
            .get().await()

        val list = mutableListOf<FriendRequest>()
        for (child in snap.children) {
            val uid = child.child("uid").getValue(String::class.java) ?: child.key.orEmpty()
            val nickname = child.child("nickname").getValue(String::class.java) ?: ""
            val username = child.child("username").getValue(String::class.java) ?: ""
            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L

            list.add(FriendRequest(uid = uid, nickname = nickname, username = username, createdAt = createdAt))
        }

        return list.sortedByDescending { it.createdAt }
    }



    suspend fun acceptRequest(req: FriendRequest) {
        val me = myUid()
        val other = req.uid
        if (other.isBlank() || other == me) return

        val root = db.reference
        val now = System.currentTimeMillis()

        val meSnap = root.child("users").child(me).get().await()
        val myNickname = meSnap.child("nickname").getValue(String::class.java) ?: ""
        val myUsername = meSnap.child("username").getValue(String::class.java) ?: ""

        val otherNickname = req.nickname
        val otherUsername = req.username

        val updates = hashMapOf<String, Any?>()

        updates["users/$me/friends/$other"] = mapOf(
            "uid" to other,
            "nickname" to otherNickname,
            "username" to otherUsername,
            "createdAt" to now
        )

        updates["users/$other/friends/$me"] = mapOf(
            "uid" to me,
            "nickname" to myNickname,
            "username" to myUsername,
            "createdAt" to now
        )

        updates["users/$me/friend_requests_in/$other"] = null
        updates["users/$other/friend_requests_out/$me"] = null

        updates["users/$me/friend_requests_out/$other"] = null
        updates["users/$other/friend_requests_in/$me"] = null

        root.updateChildren(updates).await()
    }


    suspend fun declineRequest(req: FriendRequest) {
        val me = myUid()
        val other = req.uid
        if (other.isBlank() || other == me) return

        val root = db.reference

        val updates = hashMapOf<String, Any?>()
        updates["users/$me/friend_requests_in/$other"] = null
        updates["users/$other/friend_requests_out/$me"] = null

        // cleanup reverse just in case
        updates["users/$me/friend_requests_out/$other"] = null
        updates["users/$other/friend_requests_in/$me"] = null

        root.updateChildren(updates).await()
    }
}
