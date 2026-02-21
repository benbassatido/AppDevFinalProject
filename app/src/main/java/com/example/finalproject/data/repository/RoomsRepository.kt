package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.firebase.FirebasePaths
import com.example.finalproject.data.model.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomsRepository {

    private val db = FirebaseProvider.database
    private val rootRef = FirebaseProvider.databaseRef

    private val roomsRef = rootRef.child(FirebasePaths.ROOMS)
    private val membersRef = rootRef.child(FirebasePaths.ROOM_MEMBERS)
    private val userCurrentRoomRef = rootRef.child(FirebasePaths.USER_CURRENT_ROOM)

    private val auth = FirebaseProvider.auth
    private fun uid(): String = auth.currentUser?.uid.orEmpty()

    private val usersRepo = RepositoryManager.usersRepo

    // generates a unique room key from game and room parameters
    private fun makeRoomKey(gameId: String, variant: String, partyType: String, title: String): String {
        fun clean(s: String): String {
            return s.lowercase()
                .trim()
                .replace(Regex("\\s+"), "_")
                .replace(Regex("[^a-z0-9_]"), "")
        }
        return listOf(clean(gameId), clean(variant), clean(partyType), clean(title)).joinToString("_")
    }

    // creates a new room in firebase with the provided details
    fun createRoom(
        room: Room,
        onSuccess: (roomId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val ownerUid = uid()
        if (ownerUid.isBlank()) {
            onError("Not logged in")
            return
        }

        val key = makeRoomKey(room.gameId, room.variant, room.partyType, room.title)
        val roomRef = roomsRef.child(key)

        roomRef.get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    onError("Room already exists. Choose another name.")
                    return@addOnSuccessListener
                }

                val data: Map<String, Any?> = mapOf(
                    "id" to key,
                    "title" to room.title,
                    "description" to room.description,
                    "micRequired" to room.micRequired,
                    "gameId" to room.gameId,
                    "gameName" to room.gameName,
                    "variant" to room.variant,
                    "partyType" to room.partyType,
                    "maxPlayers" to room.maxPlayers,
                    "ownerUid" to ownerUid,
                    "ownerName" to room.ownerName,
                    "status" to "open",
                    "createdAt" to ServerValue.TIMESTAMP,
                    "currentPlayers" to 0
                )

                roomRef.setValue(data)
                    .addOnSuccessListener { onSuccess(key) }
                    .addOnFailureListener { e -> onError(e.message ?: "Failed to create room") }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to check room")
            }
    }


    // adds the current user to a room as a member
    fun joinRoom(
        roomId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userUid = uid()
        if (userUid.isBlank()) {
            onError("Not logged in")
            return
        }

        usersRepo.ensureUserKey(
            uid = userUid,
            onSuccess = { userKey ->

                val currentRef = userCurrentRoomRef.child(userKey)

                currentRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentRoomId = currentData.getValue(String::class.java)

                        // already in another room
                        if (!currentRoomId.isNullOrBlank() && currentRoomId != roomId) {
                            return Transaction.abort()
                        }

                        // not in any room OR already in this room
                        currentData.value = roomId
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            onError(error.message)
                            return
                        }

                        if (!committed) {
                            onError("You are already in another room. Leave it first.")
                            return
                        }

                        val memberRef = membersRef.child(roomId).child(userKey)

                        memberRef.get()
                            .addOnSuccessListener { snap ->
                                if (snap.exists()) {
                                    onSuccess()
                                    return@addOnSuccessListener
                                }

                                rootRef.child("users").child(userKey).get()
                                    .addOnSuccessListener { userSnap ->
                                        val nickname = userSnap.child("nickname").getValue(String::class.java).orEmpty()
                                        val username = userSnap.child("username").getValue(String::class.java).orEmpty()

                                        val memberData = mapOf(
                                            "nickname" to nickname,
                                            "username" to username
                                        )

                                        memberRef.setValue(memberData)
                                            .addOnSuccessListener {
                                                incrementCurrentPlayers(roomId, +1, onSuccess, onError)
                                            }
                                            .addOnFailureListener { e ->
                                                // rollback lock if membership failed
                                                currentRef.setValue(null)
                                                onError(e.message ?: "Failed to join room")
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        currentRef.setValue(null)
                                        onError(e.message ?: "Failed to read user profile")
                                    }
                            }
                            .addOnFailureListener { e ->
                                currentRef.setValue(null)
                                onError(e.message ?: "Failed to read membership")
                            }
                    }
                })
            },
            onError = onError
        )
    }

    // removes the current user from a room
    fun leaveRoom(
        roomId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userUid = uid()
        if (userUid.isBlank()) {
            onError("Not logged in")
            return
        }

        usersRepo.ensureUserKey(
            uid = userUid,
            onSuccess = { userKey ->

                val updates = hashMapOf<String, Any?>(
                    "/room_members/$roomId/$userKey" to null,
                    "/user_current_room/$userKey" to null
                )

                rootRef.updateChildren(updates)
                    .addOnSuccessListener {
                        incrementCurrentPlayers(roomId, -1, onSuccess, onError)
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to leave room")
                    }
            },
            onError = onError
        )
    }

    // updates the current player count in a room
    private fun incrementCurrentPlayers(
        roomId: String,
        delta: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val roomRef = roomsRef.child(roomId)
        val playersRef = roomRef.child("currentPlayers")

        playersRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = (currentData.value as? Long)?.toInt() ?: 0
                var next = current + delta
                if (next < 0) next = 0
                currentData.value = next
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onError(error.message)
                    return
                }

                val next = (currentData?.getValue(Long::class.java) ?: 0L).toInt()

                if (next == 0) {
                    val updates = hashMapOf<String, Any?>(
                        "/rooms/$roomId" to null,
                        "/room_members/$roomId" to null
                    )

                    rootRef.updateChildren(updates)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Failed to delete empty room")
                        }
                } else {
                    onSuccess()
                }
            }
        })
    }

}
