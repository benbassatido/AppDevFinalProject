package com.example.finalproject.data.repository

import com.example.finalproject.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    fun chatIdFor(a: String, b: String): String =
        if (a < b) "${a}_$b" else "${b}_$a"

    suspend fun ensureChat(chatId: String, uid1: String, uid2: String) {
        val root = db.reference
        val chatRef = root.child("chats").child(chatId)

        val snap = chatRef.child("participants").get().await()
        if (!snap.exists()) {
            // create participants
            val participants = mapOf(uid1 to true, uid2 to true)
            chatRef.child("participants").setValue(participants).await()
        }

        // index under each user
        val now = System.currentTimeMillis()
        root.child("users").child(uid1).child("chats").child(chatId)
            .setValue(mapOf("otherUid" to uid2, "updatedAt" to now)).await()
        root.child("users").child(uid2).child("chats").child(chatId)
            .setValue(mapOf("otherUid" to uid1, "updatedAt" to now)).await()
    }

    suspend fun sendMessage(chatId: String, text: String) {
        val msgText = text.trim()
        if (msgText.isEmpty()) return

        val root = db.reference
        val msgRef = root.child("chats").child(chatId).child("messages").push()
        val now = System.currentTimeMillis()

        val msg = ChatMessage(
            senderId = myUid(),
            text = msgText,
            createdAt = now
        )
        msgRef.setValue(msg).await()


    }


     // Live updates of messages (last N)

    fun listenMessages(
        chatId: String,
        limit: Int = 50,
        onMessages: (List<ChatMessage>) -> Unit,
        onError: (DatabaseError) -> Unit
    ): ValueEventListener {
        val ref = db.reference.child("chats").child(chatId)
            .child("messages")
            .orderByChild("createdAt")
            .limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                    val text = child.child("text").getValue(String::class.java) ?: ""
                    val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
                    list.add(ChatMessage(senderId, text, createdAt))
                }
                onMessages(list.sortedBy { it.createdAt })
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        }

        ref.addValueEventListener(listener)
        return listener
    }

    fun stopListening(chatId: String, listener: ValueEventListener) {
        db.reference.child("chats").child(chatId).child("messages")
            .removeEventListener(listener)
    }
}
