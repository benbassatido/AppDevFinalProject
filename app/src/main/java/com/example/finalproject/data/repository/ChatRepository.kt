package com.example.finalproject.data.repository

import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.firebase.FirebasePaths
import com.example.finalproject.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseDatabase = FirebaseProvider.database,
    auth: FirebaseAuth = FirebaseProvider.auth,
    usersRepo: UsersRepository = RepositoryManager.usersRepo
) : BaseRepository(auth, usersRepo) {

    // generates a unique chat id from two user keys
    fun chatIdFor(aUserKey: String, bUserKey: String): String =
        if (aUserKey < bUserKey) "${aUserKey}_$bUserKey" else "${bUserKey}_$aUserKey"

    // ensures a chat exists between two users in firebase
    suspend fun ensureChat(chatId: String, userKey1: String, userKey2: String) {
        val root = db.reference
        val chatRef = root.child(FirebasePaths.CHATS).child(chatId)

        val snap = chatRef.child(FirebasePaths.PARTICIPANTS).get().await()
        if (!snap.exists()) {
            val participants = mapOf(userKey1 to true, userKey2 to true)
            chatRef.child(FirebasePaths.PARTICIPANTS).setValue(participants).await()
        }

        val now = System.currentTimeMillis()

        root.child(FirebasePaths.USERS).child(userKey1).child(FirebasePaths.CHATS_LIST).child(chatId)
            .setValue(mapOf(FirebasePaths.OTHER_USER_KEY to userKey2, FirebasePaths.UPDATED_AT to now)).await()

        root.child(FirebasePaths.USERS).child(userKey2).child(FirebasePaths.CHATS_LIST).child(chatId)
            .setValue(mapOf(FirebasePaths.OTHER_USER_KEY to userKey1, FirebasePaths.UPDATED_AT to now)).await()
    }

    // sends a text message to a specific chat
    suspend fun sendMessage(chatId: String, text: String) {
        val msgText = text.trim()
        if (msgText.isEmpty()) return

        val uid = myUid()
        val myUserKey = usersRepo.ensureUserKeySuspend(uid)

        val root = db.reference
        val msgRef = root.child(FirebasePaths.chatMessagesPath(chatId)).push()
        val now = System.currentTimeMillis()

        val msg = ChatMessage(
            senderId = myUserKey,
            text = msgText,
            createdAt = now
        )
        msgRef.setValue(msg).await()
    }

    // real time chat message updates from firebase
    fun listenMessages(
        chatId: String,
        limit: Int = 50,
        onMessages: (List<ChatMessage>) -> Unit,
        onError: (DatabaseError) -> Unit
    ): ValueEventListener {
        val ref = db.reference.child(FirebasePaths.chatMessagesPath(chatId))
            .orderByChild(FirebasePaths.CREATED_AT)
            .limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val senderId = child.child(FirebasePaths.SENDER_ID).getValue(String::class.java) ?: ""
                    val text = child.child(FirebasePaths.TEXT).getValue(String::class.java) ?: ""
                    val createdAt = child.child(FirebasePaths.CREATED_AT).getValue(Long::class.java) ?: 0L
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

    // stops listening to chat message updates
    fun stopListening(chatId: String, listener: ValueEventListener) {
        db.reference.child(FirebasePaths.chatMessagesPath(chatId))
            .removeEventListener(listener)
    }
}
