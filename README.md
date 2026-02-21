# 🎮 SquadApp

> **Your Ultimate Gaming Squad Finder** - Connect with gamers, form squads, and dominate together!

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)
![Material Design](https://img.shields.io/badge/Design-Material%20Design-blue.svg)

</div>

---

## 📋 Project Overview

**SquadApp** is a modern Android social networking application designed specifically for gamers. It helps players find teammates, create gaming rooms, and connect with like-minded gamers across multiple popular titles. Whether you're looking for a duo partner in Fortnite or a full squad for Valorant, SquadApp makes it easy to find and connect with the perfect teammates.

### 🎯 Why SquadApp?

- **No More Solo Queue** - Find dedicated teammates who match your playstyle
- **Multi-Game Support** - Connect across your favorite games
- **Real-Time Communication** - Built-in chat to coordinate strategies
- **Skill-Based Matching** - Filter by rank, level, and game mode preferences

---

## ✨ Features

### 🔐 **Authentication System**
- Email & Password authentication
- Google Sign-In integration
- Secure user profile management
- Custom username and nickname setup

### 👥 **Friends & Social**
- Send and receive friend requests
- Manage your friends list
- Search for users by nickname
- Real-time friend status updates
- Remove friends with confirmation

### 🎯 **Room Management**
- Create custom gaming rooms
- Specify game, variant, and party type
- Set microphone requirements
- Room descriptions and player limits
- Auto-cleanup when rooms are empty
- View and join available rooms

### 💬 **Real-Time Chat**
- One-on-one messaging with friends
- Real-time message delivery
- Message history
- Clean, modern chat interface

### 🕹️ **Multi-Game Support**
- **Fortnite** - Build/No Build modes, Duo/Trio/Squad
- **Counter-Strike 2** - Faceit levels, Premier rankings
- **Valorant** - Rank-based matchmaking (Iron to Radiant)
- **Arc Raiders** - Multiple map support
- **Battlefield 6** - Battle Royale & Multiplayer modes
- **Call of Duty: Black Ops 7** - Warzone & Multiplayer

### 👤 **User Profile**
- Customizable nickname and username
- Profile completion tracking
- User statistics
- Account management

---

## 🛠️ Technologies Used

### **Core Technologies**
- **Language**: Kotlin
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Architecture**: Repository Pattern

### **Firebase Services**
- **Firebase Authentication** - User management and authentication
- **Firebase Realtime Database** - Real-time data synchronization
- **Firebase Security Rules** - Data protection and access control

### **Android Components**
- **Material Design 3** - Modern UI components
- **RecyclerView** - Efficient list rendering
- **ConstraintLayout** - Flexible UI layouts
- **Navigation Component** - Fragment navigation
- **ViewBinding** - Type-safe view access
- **Coroutines** - Asynchronous programming

### **Key Libraries**
```gradle
// Firebase
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-database'
implementation 'com.google.android.gms:play-services-auth'

// Material Design
implementation 'com.google.android.material:material:1.12.0'

// Kotlin Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services'
```

---

## 📥 Installation

### **Prerequisites**
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 34
- Firebase account and project setup

### **Setup Steps**

1. **Clone the repository**
   ```bash
   git clone https://github.com/benbassatido/AppDevFinalProject
   cd squadapp
   ```

2. **Firebase Configuration**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Firebase Authentication (Email/Password and Google Sign-In)
   - Enable Firebase Realtime Database
   - Download `google-services.json`
   - Place it in the `app/` directory

3. **Configure Firebase Database Rules**
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```

4. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

5. **Configure Google Sign-In**
   - Go to Firebase Console → Authentication → Sign-in method
   - Enable Google Sign-In
   - Add your SHA-1 certificate fingerprint
   - Download updated `google-services.json`

---

## 🚀 How to Run

### **Running on Emulator**

1. **Create an AVD (Android Virtual Device)**
   - Open AVD Manager in Android Studio
   - Create a new device (recommended: Pixel 5 with API 34)
   - Start the emulator

2. **Run the app**
   - Click the "Run" button (▶️) in Android Studio
   - Or use the command line:
     ```bash
     ./gradlew installDebug
     ```

### **Running on Physical Device**

1. **Enable Developer Options**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Enable "USB Debugging" in Developer Options

2. **Connect Device**
   - Connect your Android device via USB
   - Accept USB debugging prompt on device

3. **Run the app**
   - Select your device in Android Studio
   - Click "Run" (▶️)

### **Build APK**
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing configuration)
./gradlew assembleRelease
```

---

## 📁 Project Structure

```
finalProject2/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/finalproject/
│   │   │   │   │
│   │   │   │   ├── data/
│   │   │   │   │   ├── firebase/
│   │   │   │   │   │   ├── FirebaseProvider.kt       # Firebase instances
│   │   │   │   │   │   └── FirebasePaths.kt          # Database path constants
│   │   │   │   │   │
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── User.kt                   # User data model
│   │   │   │   │   │   ├── Room.kt                   # Room data model
│   │   │   │   │   │   ├── Friend.kt                 # Friend data model
│   │   │   │   │   │   ├── ChatMessage.kt            # Message data model
│   │   │   │   │   │   └── Game.kt                   # Game data model
│   │   │   │   │   │
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── AuthHelper.kt             # Authentication helper
│   │   │   │   │       ├── UsersRepository.kt        # User operations
│   │   │   │   │       ├── RoomsRepository.kt        # Room management
│   │   │   │   │       ├── FriendsRepository.kt      # Friend operations
│   │   │   │   │       ├── ChatRepository.kt         # Chat operations
│   │   │   │   │       └── GamesRepository.kt        # Game data provider
│   │   │   │   │
│   │   │   │   └── ui/
│   │   │   │       ├── auth/
│   │   │   │       │   ├── AuthActivity.kt           # Authentication flow
│   │   │   │       │   ├── LoginFragment.kt          # Login screen
│   │   │   │       │   ├── RegisterFragment.kt       # Registration screen
│   │   │   │       │   └── CompleteProfileFragment.kt # Profile setup
│   │   │   │       │
│   │   │   │       ├── home/
│   │   │   │       │   ├── HomeFragment.kt           # Games grid
│   │   │   │       │   └── GamesAdapter.kt           # Games adapter
│   │   │   │       │
│   │   │   │       ├── games/
│   │   │   │       │   ├── GameDetailsFragment.kt    # Game variants
│   │   │   │       │   └── GameVariantFragment.kt    # Party type selection
│   │   │   │       │
│   │   │   │       ├── rooms/
│   │   │   │       │   ├── RoomsFragment.kt          # Available rooms
│   │   │   │       │   ├── RoomFragment.kt           # Room details
│   │   │   │       │   ├── RoomsAdapter.kt           # Rooms list adapter
│   │   │   │       │   └── create/
│   │   │   │       │       └── CreateRoomFragment.kt # Room creation
│   │   │   │       │
│   │   │   │       ├── friends/
│   │   │   │       │   ├── FriendsFragment.kt        # Friends management
│   │   │   │       │   ├── adapters/
│   │   │   │       │   │   ├── FriendsAdapter.kt     # Friends list
│   │   │   │       │   │   └── SearchResultsAdapter.kt # Search results
│   │   │   │       │   └── requests/
│   │   │   │       │       ├── FriendRequestsFragment.kt
│   │   │   │       │       └── FriendRequestsAdapter.kt
│   │   │   │       │
│   │   │   │       ├── chat/
│   │   │   │       │   ├── ChatFragment.kt           # Chat interface
│   │   │   │       │   └── MessagesAdapter.kt        # Messages adapter
│   │   │   │       │
│   │   │   │       ├── profile/
│   │   │   │       │   └── ProfileFragment.kt        # User profile
│   │   │   │       │
│   │   │   │       └── common/
│   │   │   │           ├── ErrorHandler.kt           # Error handling utility
│   │   │   │           └── ViewFactory.kt            # UI component factory
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/                           # XML layouts
│   │   │   │   ├── drawable/                         # Icons and backgrounds
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml                   # String resources
│   │   │   │   │   ├── colors.xml                    # Color palette
│   │   │   │   │   ├── dimens.xml                    # Dimension resources
│   │   │   │   │   └── styles.xml                    # Style definitions
│   │   │   │   └── navigation/                       # Navigation graphs
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── google-services.json                      # Firebase configuration
│   │
│   └── build.gradle                                  # App-level build config
│
├── gradle/                                           # Gradle wrapper
├── build.gradle                                      # Project-level build config
└── README.md                                         # This file
```

### **Key Architecture Patterns**

- **Repository Pattern**: Separates data access logic from business logic
- **Single Activity Architecture**: MainActivity hosts all fragments
- **Observer Pattern**: Real-time database listeners for live updates
- **Factory Pattern**: ViewFactory for dynamic UI component creation

---

## 🎨 Design Principles

### **UI/UX Features**
- ✨ Material Design 3 components
- 🎨 Custom color scheme with gradient backgrounds
- 📱 Responsive layouts for all screen sizes
- ♿ Accessibility-friendly components
- 🌙 Clean, modern aesthetic

### **Color Palette**
```xml
<!-- Primary Colors -->
#8BA7F5 - Primary Blue (Buttons, accents)
#7DCCB8 - Secondary Green (Success actions)
#C7D5F5 - Light Blue (Backgrounds)

<!-- Text Colors -->
#2C3E50 - Dark Gray (Primary text)
#6B7280 - Medium Gray (Secondary text)
#9CA3AF - Light Gray (Hints, placeholders)

<!-- Accent Colors -->
#EF4444 - Red (Delete, errors)
#F87171 - Light Red (Warnings)
```

---

## 🔒 Security Features

- Firebase Authentication for secure user management
- Email verification support
- Secure password handling
- User data encryption via Firebase
- Protected API endpoints with auth rules
- Transaction-based room joining (prevents race conditions)

---

## 🐛 Known Issues & Future Improvements

### **Known Issues**
- None currently reported

### **Planned Features**
- [ ] Push notifications for friend requests and messages
- [ ] Voice chat integration
- [ ] User profiles with stats and achievements
- [ ] Room invite system
- [ ] Block user functionality
- [ ] Report abuse system
- [ ] Dark mode support
- [ ] Multiple language support

---

## 📄 License

This project is created for educational purposes as part of a Computer Science degree program.

---

## 👨‍💻 Author

**[Ido Ben Bassat]**
- GitHub: [@benbassatido](https://github.com/benbassatido)
- LinkedIn: [Ido Ben Bassat](https://www.linkedin.com/in/ido-ben-bassat-bb8908317/)
- Email: benbassatido@gmail.com

---

## 🙏 Acknowledgments

- Firebase for backend infrastructure
- Material Design team for UI components
- Android Developer community for resources and support
- All contributors and testers

---

<div align="center">

### ⭐ If you found this project helpful, please consider giving it a star!

**Made with ❤️ by **[Ido Ben Bassat]**

</div>
