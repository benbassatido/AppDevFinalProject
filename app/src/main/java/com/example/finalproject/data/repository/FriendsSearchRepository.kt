package com.example.finalproject.data.repository

import com.example.finalproject.data.model.SearchUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendsSearchRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    suspend fun searchByNicknamePrefix(prefix: String): List<SearchUser> {
        val p = prefix.trim().lowercase()
        if (p.isEmpty()) return emptyList()

        val me = myUid()

        val query = db.reference.child("users")
            .orderByChild("nicknameLower")
            .startAt(p)
            .endAt(p + "\uf8ff")
            .limitToFirst(20)

        val snap = query.get().await()

        val results = mutableListOf<SearchUser>()
        for (child in snap.children) {
            val uid = child.child("uid").getValue(String::class.java) ?: child.key.orEmpty()
            if (uid == me) continue

            results.add(
                SearchUser(
                    uid = uid,
                    username = child.child("username").getValue(String::class.java) ?: "",
                    nickname = child.child("nickname").getValue(String::class.java) ?: "",
                    nicknameLower = child.child("nicknameLower").getValue(String::class.java) ?: ""
                )
            )
        }
        return results
    }


    suspend fun sendFriendRequest(toUid: String) {
        val fromUid = myUid()
        if (toUid == fromUid) return

        val root = db.reference

        val myFriendsRef = root.child("users").child(fromUid).child("friends")

        val existsByKey = myFriendsRef.child(toUid).get().await().exists()
        val existsByUidField = myFriendsRef.orderByChild("uid").equalTo(toUid).get().await().exists()
        if (existsByKey || existsByUidField) return

        val alreadySent = root.child("users").child(fromUid)
            .child("friend_requests_out").child(toUid)
            .get().await().exists()
        if (alreadySent) return

        val iHaveFromHim = root.child("users").child(fromUid)
            .child("friend_requests_in").child(toUid)
            .get().await().exists()

        val now = System.currentTimeMillis()

        if (iHaveFromHim) {
            // Auto-accept: add friends both sides + cleanup requests both sides
            val meSnap = root.child("users").child(fromUid).get().await()
            val myNickname = meSnap.child("nickname").getValue(String::class.java) ?: ""
            val myUsername = meSnap.child("username").getValue(String::class.java) ?: ""

            val himSnap = root.child("users").child(toUid).get().await()
            val hisNickname = himSnap.child("nickname").getValue(String::class.java) ?: ""
            val hisUsername = himSnap.child("username").getValue(String::class.java) ?: ""

            val updates = hashMapOf<String, Any?>()

            updates["users/$fromUid/friends/$toUid"] = mapOf(
                "uid" to toUid,
                "nickname" to hisNickname,
                "username" to hisUsername,
                "createdAt" to now
            )
            updates["users/$toUid/friends/$fromUid"] = mapOf(
                "uid" to fromUid,
                "nickname" to myNickname,
                "username" to myUsername,
                "createdAt" to now
            )

            // cleanup requests in/out both directions
            updates["users/$fromUid/friend_requests_in/$toUid"] = null
            updates["users/$fromUid/friend_requests_out/$toUid"] = null
            updates["users/$toUid/friend_requests_in/$fromUid"] = null
            updates["users/$toUid/friend_requests_out/$fromUid"] = null

            root.updateChildren(updates).await()
            return
        }

        val fromSnap = root.child("users").child(fromUid).get().await()
        val fromNickname = fromSnap.child("nickname").getValue(String::class.java) ?: ""
        val fromUsername = fromSnap.child("username").getValue(String::class.java) ?: ""

        val incomingData = mapOf(
            "uid" to fromUid,
            "nickname" to fromNickname,
            "username" to fromUsername,
            "createdAt" to now
        )

        val outgoingData = mapOf(
            "uid" to toUid,
            "createdAt" to now
        )

        val updates = hashMapOf<String, Any?>()
        updates["users/$toUid/friend_requests_in/$fromUid"] = incomingData
        updates["users/$fromUid/friend_requests_out/$toUid"] = outgoingData

        root.updateChildren(updates).await()
    }
    suspend fun getMyOutgoingRequestsSet(): Set<String> {
        val fromUid = myUid()
        val snap = db.reference.child("users").child(fromUid)
            .child("friend_requests_out")
            .get().await()

        val set = mutableSetOf<String>()
        for (child in snap.children) {
            val toUid = child.key ?: continue
            set.add(toUid)
        }
        return set
    }

}
