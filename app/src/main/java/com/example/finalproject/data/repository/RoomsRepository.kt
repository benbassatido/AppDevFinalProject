package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomsRepository {

    private val db = FirebaseDatabase.getInstance()
    private val roomsRef = db.getReference("rooms")
    private val membersRef = db.getReference("room_members")

    private val auth = FirebaseAuth.getInstance()
    private fun uid(): String = auth.currentUser?.uid ?: ""

    fun listenToRooms(
        gameId: String? = null,
        variant: String? = null,
        partyType: String? = null,
        onResult: (List<Room>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {


        val query: Query =
            if (!gameId.isNullOrBlank()) roomsRef.orderByChild("gameId").equalTo(gameId)
            else roomsRef

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Room>()
                for (child in snapshot.children) {
                    val room = child.getValue(Room::class.java) ?: continue
                    if (room.status != "open") continue

                    if (!variant.isNullOrBlank() && room.variant != variant) continue
                    if (!partyType.isNullOrBlank() && room.partyType != partyType) continue

                    list.add(room)
                }
                // הכי חדשים למעלה
                list.sortByDescending { it.createdAt }
                onResult(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        query.addValueEventListener(listener)
        return listener
    }

    fun stopListening(queryListenerOwnerRef: Query, listener: ValueEventListener) {
        queryListenerOwnerRef.removeEventListener(listener)
    }

    fun createRoom(
        room: Room,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val myUid = uid()
        if (myUid.isBlank()) {
            onError("User not logged in")
            return
        }

        val newId = roomsRef.push().key
        if (newId == null) {
            onError("Failed to create room id")
            return
        }

        room.id = newId
        room.ownerUid = myUid
        room.createdAt = System.currentTimeMillis()
        room.status = "open"

        val updates = hashMapOf<String, Any?>(
            "/rooms/$newId" to room,

            "/room_members/$newId/$myUid" to true,


            "/user_current_room/$myUid" to newId
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener { onSuccess(newId) }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }


    fun joinRoom(
        roomId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onError("Not logged in")

        val db = FirebaseDatabase.getInstance().reference
        val userRoomRef = db.child("user_current_room").child(uid)
        val roomMembersRef = db.child("room_members").child(roomId).child(uid)

        userRoomRef.get()
            .addOnSuccessListener { snap ->
                val currentRoomId = snap.getValue(String::class.java)

                if (!currentRoomId.isNullOrBlank() && currentRoomId != roomId) {
                    onError("You are already in another room")
                    return@addOnSuccessListener
                }

                userRoomRef.setValue(roomId)
                    .addOnSuccessListener {
                        roomMembersRef.setValue(true)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError(e.message ?: "Join failed") }
                    }
                    .addOnFailureListener { e -> onError(e.message ?: "Join failed") }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to check current room")
            }
    }


    fun leaveRoom(
        roomId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val myUid = uid()
        if (myUid.isBlank()) {
            onError("User not logged in")
            return
        }

        val updates = hashMapOf<String, Any?>(
            "/room_members/$roomId/$myUid" to null,
            "/user_current_room/$myUid" to null
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Leave failed") }
    }

}
