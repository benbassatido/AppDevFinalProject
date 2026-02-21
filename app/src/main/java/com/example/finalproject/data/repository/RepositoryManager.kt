package com.example.finalproject.data.repository


object RepositoryManager {
    
    // Shared repository instances (created only once)
    val usersRepo: UsersRepository by lazy { UsersRepository() }
    
    val friendsRepo: FriendsRepository by lazy { 
        FriendsRepository(usersRepo = usersRepo) 
    }
    
    val friendsSearchRepo: FriendsSearchRepository by lazy { 
        FriendsSearchRepository(usersRepo = usersRepo) 
    }
    
    val friendRequestsRepo: FriendRequestsRepository by lazy { 
        FriendRequestsRepository(usersRepo = usersRepo) 
    }
    
    val chatRepo: ChatRepository by lazy { 
        ChatRepository(usersRepo = usersRepo) 
    }
    
    val roomsRepo: RoomsRepository by lazy { RoomsRepository() }
    
    val gamesRepo: GamesRepository = GamesRepository
    
    val gameOptionsRepo: GameOptionsRepository = GameOptionsRepository
}
