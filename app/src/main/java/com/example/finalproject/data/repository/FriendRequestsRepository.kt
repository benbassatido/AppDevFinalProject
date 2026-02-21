package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.firebase.FirebasePaths
import com.example.finalproject.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendRequestsRepository(
    private val db: FirebaseDatabase = FirebaseProvider.database,
    auth: FirebaseAuth = FirebaseProvider.auth,
    usersRepo: UsersRepository = RepositoryManager.usersRepo
) : BaseRepository(auth, usersRepo) {

    // retrieves incoming friend requests for the current user
    suspend fun getIncomingRequests(): List<User> {
        val meKey = myUserKey()

        val snap = db.reference.child(FirebasePaths.USERS).child(meKey)
            .child(FirebasePaths.FRIEND_REQUESTS_IN)
            .get().await()

        val list = mutableListOf<User>()
        for (child in snap.children) {
            val otherKey = child.key.orEmpty()
            val nickname = child.child(FirebasePaths.NICKNAME).getValue(String::class.java) ?: ""
            val username = child.child(FirebasePaths.USERNAME).getValue(String::class.java) ?: ""
            val createdAt = child.child(FirebasePaths.CREATED_AT).getValue(Long::class.java) ?: 0L
            list.add(User(userKey = otherKey, nickname = nickname, username = username, createdAt = createdAt))
        }
        return list.sortedByDescending { it.createdAt }
    }

    // accepts a friend request and adds the user as a friend
    suspend fun acceptRequest(req: User) {
        val meKey = myUserKey()
        val otherKey = req.userKey
        if (otherKey.isBlank() || otherKey == meKey) return

        val root = db.reference

        val meSnap = root.child(FirebasePaths.userPath(meKey)).get().await()
        val myNickname = meSnap.child(FirebasePaths.NICKNAME).getValue(String::class.java) ?: ""
        val myUsername = meSnap.child(FirebasePaths.USERNAME).getValue(String::class.java) ?: ""

        FriendRequestHelper.acceptFriendRequest(
            root = root,
            fromUserKey = otherKey,
            fromNickname = req.nickname,
            fromUsername = req.username,
            toUserKey = meKey,
            toNickname = myNickname,
            toUsername = myUsername
        )
    }

    // declines a friend request by removing it from the database
    suspend fun declineRequest(req: User) {
        val meKey = myUserKey()
        val otherKey = req.userKey
        if (otherKey.isBlank() || otherKey == meKey) return

        val root = db.reference
        
        FriendRequestHelper.cleanupFriendRequests(root, meKey, otherKey)
    }
}
