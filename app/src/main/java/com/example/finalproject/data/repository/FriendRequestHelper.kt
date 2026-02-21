package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebasePaths
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

object FriendRequestHelper {
    
    // creates a friend data map with user information and timestamp
    fun createFriendDataMap(
        userKey: String,
        nickname: String,
        username: String,
        timestamp: Long
    ): Map<String, Any> {
        return mapOf(
            "userKey" to userKey,
            FirebasePaths.NICKNAME to nickname,
            FirebasePaths.USERNAME to username,
            FirebasePaths.CREATED_AT to timestamp
        )
    }
    
    // removes all friend request data between two users
    suspend fun cleanupFriendRequests(
        root: DatabaseReference,
        userKey1: String,
        userKey2: String
    ) {
        if (userKey1.isBlank() || userKey2.isBlank() || userKey1 == userKey2) {
            return
        }
        
        val updates = hashMapOf<String, Any?>(
            "${FirebasePaths.userRequestsInPath(userKey1)}/$userKey2" to null,
            "${FirebasePaths.userRequestsOutPath(userKey1)}/$userKey2" to null,
            "${FirebasePaths.userRequestsInPath(userKey2)}/$userKey1" to null,
            "${FirebasePaths.userRequestsOutPath(userKey2)}/$userKey1" to null
        )
        
        root.updateChildren(updates).await()
    }
    
    // adds two users as mutual friends in firebase
    suspend fun addMutualFriends(
        root: DatabaseReference,
        userKey1: String,
        nickname1: String,
        username1: String,
        userKey2: String,
        nickname2: String,
        username2: String,
        timestamp: Long
    ) {
        val updates = hashMapOf<String, Any?>(
            "${FirebasePaths.userFriendsPath(userKey1)}/$userKey2" to createFriendDataMap(
                userKey2, nickname2, username2, timestamp
            ),
            "${FirebasePaths.userFriendsPath(userKey2)}/$userKey1" to createFriendDataMap(
                userKey1, nickname1, username1, timestamp
            )
        )
        
        root.updateChildren(updates).await()
    }
    
    // accepts a friend request and establishes mutual friendship
    suspend fun acceptFriendRequest(
        root: DatabaseReference,
        fromUserKey: String,
        fromNickname: String,
        fromUsername: String,
        toUserKey: String,
        toNickname: String,
        toUsername: String
    ) {
        val now = System.currentTimeMillis()
        
        // Add as mutual friends
        addMutualFriends(
            root = root,
            userKey1 = fromUserKey,
            nickname1 = fromNickname,
            username1 = fromUsername,
            userKey2 = toUserKey,
            nickname2 = toNickname,
            username2 = toUsername,
            timestamp = now
        )
        
        // Clean up requests
        cleanupFriendRequests(root, fromUserKey, toUserKey)
    }
}
