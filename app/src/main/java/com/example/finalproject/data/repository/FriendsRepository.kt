package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.firebase.FirebasePaths
import com.example.finalproject.data.model.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FriendsRepository(
    private val db: FirebaseDatabase = FirebaseProvider.database,
    auth: FirebaseAuth = FirebaseProvider.auth,
    usersRepo: UsersRepository = RepositoryManager.usersRepo
) : BaseRepository(auth, usersRepo) {

    // retrieves the list of friends for the current user
    suspend fun getFriends(): List<Friend> {
        val meKey = myUserKey()

        val snap = db.reference
            .child(FirebasePaths.USERS).child(meKey)
            .child(FirebasePaths.FRIENDS)
            .get().await()

        val list = mutableListOf<Friend>()
        for (child in snap.children) {
            val friendKey = child.key.orEmpty()
            val nickname = child.child(FirebasePaths.NICKNAME).getValue(String::class.java) ?: ""
            val username = child.child(FirebasePaths.USERNAME).getValue(String::class.java) ?: ""
            val createdAt = child.child(FirebasePaths.CREATED_AT).getValue(Long::class.java) ?: 0L

            list.add(Friend(userKey = friendKey, nickname = nickname, username = username, createdAt = createdAt))
        }

        return list.sortedBy { it.nickname.lowercase() }
    }

    // removes a friend and cleans up any related friend requests
    suspend fun removeFriendMutualAndCleanupRequests(friendUserKey: String) {
        val meKey = myUserKey()
        if (friendUserKey.isBlank() || friendUserKey == meKey) return

        val root = db.reference

        val myFriendsRef = root.child(FirebasePaths.USERS).child(meKey).child(FirebasePaths.FRIENDS)
        val hisFriendsRef = root.child(FirebasePaths.USERS).child(friendUserKey).child(FirebasePaths.FRIENDS)

        removeFriendFromRef(myFriendsRef, friendUserKey)
        removeFriendFromRef(hisFriendsRef, meKey)

        FriendRequestHelper.cleanupFriendRequests(root, meKey, friendUserKey)
    }

    // removes a specific friend from the friends list
    private suspend fun removeFriendFromRef(friendsRef: DatabaseReference, targetKey: String) {
        friendsRef.child(targetKey).removeValue().await()
    }
}
