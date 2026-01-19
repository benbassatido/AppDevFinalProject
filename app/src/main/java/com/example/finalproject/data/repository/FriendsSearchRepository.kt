package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.SearchUserUi
import com.example.finalproject.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendsSearchRepository(
    private val db: FirebaseDatabase = FirebaseProvider.database,
    auth: FirebaseAuth = FirebaseProvider.auth,
    usersRepo: UsersRepository = UsersRepository()
) : BaseRepository(auth, usersRepo) {

    companion object {
        private const val SEARCH_RESULTS_LIMIT = 20
    }

    suspend fun searchByNicknamePrefix(prefix: String): List<SearchUserUi> {
        val p = prefix.trim().lowercase()
        if (p.isEmpty()) return emptyList()

        val meKey = myUserKey()

        val query = db.reference.child("users")
            .orderByChild("nicknameLower")
            .startAt(p)
            .endAt(p + "\uf8ff")
            .limitToFirst(SEARCH_RESULTS_LIMIT)

        val snap = query.get().await()

        val results = mutableListOf<SearchUserUi>()
        for (child in snap.children) {
            val userKey = child.key.orEmpty()
            if (userKey == meKey) continue

            val user = User(
                userKey = userKey,
                uid = child.child("uid").getValue(String::class.java) ?: "",
                username = child.child("username").getValue(String::class.java) ?: "",
                nickname = child.child("nickname").getValue(String::class.java) ?: "",
                nicknameLower = child.child("nicknameLower").getValue(String::class.java) ?: "",
                email = child.child("email").getValue(String::class.java) ?: "",
                createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
            )
            results.add(SearchUserUi(user = user))
        }
        
        return results
    }

    suspend fun sendFriendRequest(toUserKey: String) {
        val fromUserKey = myUserKey()
        if (toUserKey == fromUserKey) return

        val root = db.reference

        val myFriendsRef = root.child("users").child(fromUserKey).child("friends")
        val exists = myFriendsRef.child(toUserKey).get().await().exists()
        if (exists) return

        val alreadySent = root.child("users").child(fromUserKey)
            .child("friend_requests_out").child(toUserKey)
            .get().await().exists()
        if (alreadySent) return

        val iHaveFromHim = root.child("users").child(fromUserKey)
            .child("friend_requests_in").child(toUserKey)
            .get().await().exists()

        val now = System.currentTimeMillis()

        if (iHaveFromHim) {
            // auto-accept
            val meSnap = root.child("users").child(fromUserKey).get().await()
            val myNickname = meSnap.child("nickname").getValue(String::class.java) ?: ""
            val myUsername = meSnap.child("username").getValue(String::class.java) ?: ""

            val himSnap = root.child("users").child(toUserKey).get().await()
            val hisNickname = himSnap.child("nickname").getValue(String::class.java) ?: ""
            val hisUsername = himSnap.child("username").getValue(String::class.java) ?: ""

            val updates = hashMapOf<String, Any?>()

            updates["users/$fromUserKey/friends/$toUserKey"] = mapOf(
                "userKey" to toUserKey,
                "nickname" to hisNickname,
                "username" to hisUsername,
                "createdAt" to now
            )
            updates["users/$toUserKey/friends/$fromUserKey"] = mapOf(
                "userKey" to fromUserKey,
                "nickname" to myNickname,
                "username" to myUsername,
                "createdAt" to now
            )

            updates["users/$fromUserKey/friend_requests_in/$toUserKey"] = null
            updates["users/$fromUserKey/friend_requests_out/$toUserKey"] = null
            updates["users/$toUserKey/friend_requests_in/$fromUserKey"] = null
            updates["users/$toUserKey/friend_requests_out/$fromUserKey"] = null

            root.updateChildren(updates).await()
            return
        }

        val fromSnap = root.child("users").child(fromUserKey).get().await()
        val fromNickname = fromSnap.child("nickname").getValue(String::class.java) ?: ""
        val fromUsername = fromSnap.child("username").getValue(String::class.java) ?: ""

        val incomingData = mapOf(
            "nickname" to fromNickname,
            "username" to fromUsername,
            "createdAt" to now
        )

        val outgoingData = mapOf(
            "createdAt" to now
        )

        val updates = hashMapOf<String, Any?>()
        updates["users/$toUserKey/friend_requests_in/$fromUserKey"] = incomingData
        updates["users/$fromUserKey/friend_requests_out/$toUserKey"] = outgoingData

        root.updateChildren(updates).await()
    }

    suspend fun getMyOutgoingRequestsSet(): Set<String> {
        val fromKey = myUserKey()
        val snap = db.reference.child("users").child(fromKey)
            .child("friend_requests_out")
            .get().await()

        val set = mutableSetOf<String>()
        for (child in snap.children) {
            val toKey = child.key ?: continue
            set.add(toKey)
        }
        return set
    }
}
