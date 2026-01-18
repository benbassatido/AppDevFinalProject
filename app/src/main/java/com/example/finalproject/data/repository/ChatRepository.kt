package com.example.finalproject.data.repository

import com.example.finalproject.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val usersRepo: UsersRepository = UsersRepository()
) {

    private fun myUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")

    fun chatIdFor(aUserKey: String, bUserKey: String): String =
        if (aUserKey < bUserKey) "${aUserKey}_$bUserKey" else "${bUserKey}_$aUserKey"

    suspend fun ensureChat(chatId: String, userKey1: String, userKey2: String) {
        val root = db.reference
        val chatRef = root.child("chats").child(chatId)

        val snap = chatRef.child("participants").get().await()
        if (!snap.exists()) {
            val participants = mapOf(userKey1 to true, userKey2 to true)
            chatRef.child("participants").setValue(participants).await()
        }

        val now = System.currentTimeMillis()

        root.child("users").child(userKey1).child("chats").child(chatId)
            .setValue(mapOf("otherUserKey" to userKey2, "updatedAt" to now)).await()

        root.child("users").child(userKey2).child("chats").child(chatId)
            .setValue(mapOf("otherUserKey" to userKey1, "updatedAt" to now)).await()
    }

    suspend fun sendMessage(chatId: String, text: String) {
        val msgText = text.trim()
        if (msgText.isEmpty()) return

        val uid = myUid()
        val myUserKey = usersRepo.ensureUserKeySuspend(uid)

        val root = db.reference
        val msgRef = root.child("chats").child(chatId).child("messages").push()
        val now = System.currentTimeMillis()

        val msg = ChatMessage(
            senderId = myUserKey,
            text = msgText,
            createdAt = now
        )
        msgRef.setValue(msg).await()
    }

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
