package com.example.finalproject.data.firebase

object FirebasePaths {
    
    // Root level collections
    const val USERS = "users"
    const val ROOMS = "rooms"
    const val CHATS = "chats"
    const val UID_TO_USER = "uid_to_user"
    const val ROOM_MEMBERS = "room_members"
    const val USER_CURRENT_ROOM = "user_current_room"
    
    // User sub-paths
    const val FRIENDS = "friends"
    const val FRIEND_REQUESTS_IN = "friend_requests_in"
    const val FRIEND_REQUESTS_OUT = "friend_requests_out"
    const val CHATS_LIST = "chats"
    const val NICKNAME = "nickname"
    const val USERNAME = "username"
    const val NICKNAME_LOWER = "nicknameLower"
    const val EMAIL = "email"
    const val UID = "uid"
    const val CREATED_AT = "createdAt"
    
    // Room sub-paths
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val MIC_REQUIRED = "micRequired"
    const val GAME_ID = "gameId"
    const val GAME_NAME = "gameName"
    const val VARIANT = "variant"
    const val PARTY_TYPE = "partyType"
    const val MAX_PLAYERS = "maxPlayers"
    const val CURRENT_PLAYERS = "currentPlayers"
    const val OWNER_UID = "ownerUid"
    const val OWNER_NAME = "ownerName"
    const val STATUS = "status"
    
    // Chat sub-paths
    const val MESSAGES = "messages"
    const val PARTICIPANTS = "participants"
    const val SENDER_ID = "senderId"
    const val TEXT = "text"
    const val OTHER_USER_KEY = "otherUserKey"
    const val UPDATED_AT = "updatedAt"
    
    fun userPath(userKey: String) = "$USERS/$userKey"
    fun userFriendsPath(userKey: String) = "$USERS/$userKey/$FRIENDS"
    fun userRequestsInPath(userKey: String) = "$USERS/$userKey/$FRIEND_REQUESTS_IN"
    fun userRequestsOutPath(userKey: String) = "$USERS/$userKey/$FRIEND_REQUESTS_OUT"
    fun roomPath(roomId: String) = "$ROOMS/$roomId"
    fun roomMembersPath(roomId: String) = "$ROOM_MEMBERS/$roomId"
    fun chatPath(chatId: String) = "$CHATS/$chatId"
    fun chatMessagesPath(chatId: String) = "$CHATS/$chatId/$MESSAGES"
}
