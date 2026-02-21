package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.firebase.FirebasePaths
import com.example.finalproject.data.model.SearchUserUi
import com.example.finalproject.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendsSearchRepository(
    private val db: FirebaseDatabase = FirebaseProvider.database,
    auth: FirebaseAuth = FirebaseProvider.auth,
    usersRepo: UsersRepository = RepositoryManager.usersRepo
) : BaseRepository(auth, usersRepo) {

    companion object {
        private const val SEARCH_RESULTS_LIMIT = 20
    }

    // searches for users by nickname prefix and returns results
    suspend fun searchByNicknamePrefix(prefix: String): List<SearchUserUi> {
        val p = prefix.trim().lowercase()
        if (p.isEmpty()) return emptyList()

        val meKey = myUserKey()

        val query = db.reference.child(FirebasePaths.USERS)
            .orderByChild(FirebasePaths.NICKNAME_LOWER)
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
                uid = child.child(FirebasePaths.UID).getValue(String::class.java) ?: "",
                username = child.child(FirebasePaths.USERNAME).getValue(String::class.java) ?: "",
                nickname = child.child(FirebasePaths.NICKNAME).getValue(String::class.java) ?: "",
                nicknameLower = child.child(FirebasePaths.NICKNAME_LOWER).getValue(String::class.java) ?: "",
                email = child.child(FirebasePaths.EMAIL).getValue(String::class.java) ?: "",
                createdAt = child.child(FirebasePaths.CREATED_AT).getValue(Long::class.java) ?: 0L
            )
            results.add(SearchUserUi(user = user))
        }
        
        return results
    }

    // sends a friend request to another user or auto-accepts mutual requests
    suspend fun sendFriendRequest(toUserKey: String) {
        val fromUserKey = myUserKey()
        if (toUserKey == fromUserKey) return

        val root = db.reference

        val myFriendsRef = root.child(FirebasePaths.USERS).child(fromUserKey).child(FirebasePaths.FRIENDS)
        val exists = myFriendsRef.child(toUserKey).get().await().exists()
        if (exists) return

        val alreadySent = root.child(FirebasePaths.USERS).child(fromUserKey)
            .child(FirebasePaths.FRIEND_REQUESTS_OUT).child(toUserKey)
            .get().await().exists()
        if (alreadySent) return

        val iHaveFromHim = root.child(FirebasePaths.USERS).child(fromUserKey)
            .child(FirebasePaths.FRIEND_REQUESTS_IN).child(toUserKey)
            .get().await().exists()

        if (iHaveFromHim) {
            val meSnap = root.child(FirebasePaths.userPath(fromUserKey)).get().await()
            val myNickname = meSnap.child(FirebasePaths.NICKNAME).getValue(String::class.java) ?: ""
            val myUsername = meSnap.child(FirebasePaths.USERNAME).getValue(String::class.java) ?: ""

            val himSnap = root.child(FirebasePaths.userPath(toUserKey)).get().await()
            val hisNickname = himSnap.child(FirebasePaths.NICKNAME).getValue(String::class.java) ?: ""
            val hisUsername = himSnap.child(FirebasePaths.USERNAME).getValue(String::class.java) ?: ""

            FriendRequestHelper.acceptFriendRequest(
                root = root,
                fromUserKey = fromUserKey,
                fromNickname = myNickname,
                fromUsername = myUsername,
                toUserKey = toUserKey,
                toNickname = hisNickname,
                toUsername = hisUsername
            )
            return
        }

        val fromSnap = root.child(FirebasePaths.userPath(fromUserKey)).get().await()
        val fromNickname = fromSnap.child(FirebasePaths.NICKNAME).getValue(String::class.java) ?: ""
        val fromUsername = fromSnap.child(FirebasePaths.USERNAME).getValue(String::class.java) ?: ""

        val now = System.currentTimeMillis()
        
        val incomingData = mapOf(
            FirebasePaths.NICKNAME to fromNickname,
            FirebasePaths.USERNAME to fromUsername,
            FirebasePaths.CREATED_AT to now
        )

        val outgoingData = mapOf(
            FirebasePaths.CREATED_AT to now
        )

        val updates = hashMapOf<String, Any?>()
        updates["${FirebasePaths.userRequestsInPath(toUserKey)}/$fromUserKey"] = incomingData
        updates["${FirebasePaths.userRequestsOutPath(fromUserKey)}/$toUserKey"] = outgoingData

        root.updateChildren(updates).await()
    }

    // retrieves the set of user keys to whom the current user has sent friend requests
    suspend fun getMyOutgoingRequestsSet(): Set<String> {
        val fromKey = myUserKey()
        val snap = db.reference.child(FirebasePaths.USERS).child(fromKey)
            .child(FirebasePaths.FRIEND_REQUESTS_OUT)
            .get().await()

        val set = mutableSetOf<String>()
        for (child in snap.children) {
            val toKey = child.key ?: continue
            set.add(toKey)
        }
        return set
    }
}
