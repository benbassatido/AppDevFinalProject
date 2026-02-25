package com.example.finalproject.ui.rooms

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.RoomMember
import com.example.finalproject.data.repository.RoomsRepository
import com.example.finalproject.data.repository.UsersRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler

class RoomFragment : Fragment(R.layout.fragment_room) {

    private val db = FirebaseProvider.databaseRef
    private val auth = FirebaseProvider.auth
    private val roomsRepo = RepositoryManager.roomsRepo
    private val usersRepo = RepositoryManager.usersRepo

    private var roomId: String = ""

    private var roomListener: ValueEventListener? = null
    private var membersListener: ValueEventListener? = null
    private var currentRoomListener: ValueEventListener? = null

    // views
    private lateinit var tvRoomHeader: TextView
    private lateinit var tvRoomName: TextView
    private lateinit var tvRoomSub: TextView
    private lateinit var tvRoomDesc: TextView
    private lateinit var tvPlayersCount: TextView
    private lateinit var ivMic: ImageView
    private lateinit var tvMicRequired: TextView
    private lateinit var rvMembers: RecyclerView
    private lateinit var tvEmptyMembers: TextView

    private lateinit var btnJoin: MaterialButton
    private lateinit var btnLeave: MaterialButton

    private lateinit var adapter: RoomMembersAdapter

    private var maxPlayers: Int = 0
    private var isInThisRoom: Boolean = false

    private var myUserKey: String? = null
    private var currentRoomRef: DatabaseReference? = null

    // initializes the room details screen with all necessary listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomId = requireArguments().getString("roomId").orEmpty()
        if (roomId.isBlank()) {
            ErrorHandler.showError(requireContext(), null, "Missing roomId")
            parentFragmentManager.popBackStack()
            return
        }

        tvRoomHeader = view.findViewById(R.id.tvRoomHeader)
        tvRoomName = view.findViewById(R.id.tvRoomName)
        tvRoomSub = view.findViewById(R.id.tvRoomSub)
        tvRoomDesc = view.findViewById(R.id.tvRoomDesc)
        tvPlayersCount = view.findViewById(R.id.tvPlayersCount)
        ivMic = view.findViewById(R.id.ivMic)
        tvMicRequired = view.findViewById(R.id.tvMicRequired)
        rvMembers = view.findViewById(R.id.rvMembers)
        tvEmptyMembers = view.findViewById(R.id.tvEmptyMembers)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnJoin = view.findViewById(R.id.btnReady)
        btnLeave = view.findViewById(R.id.btnLeave)

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        btnJoin.text = "JOIN ROOM"

        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) {
            setupMembersAdapter(null)
            listenRoomDetails()
            listenRoomMembers()
            listenToMyCurrentRoom(null)
        } else {
            usersRepo.ensureUserKey(
                uid = uid,
                onSuccess = { userKey ->
                    myUserKey = userKey
                    setupMembersAdapter(userKey)
                    listenRoomDetails()
                    listenRoomMembers()
                    listenToMyCurrentRoom(userKey)
                },
                onError = {
                    setupMembersAdapter(null)
                    listenRoomDetails()
                    listenRoomMembers()
                    listenToMyCurrentRoom(null)
                }
            )
        }

        btnJoin.setOnClickListener {
            roomsRepo.joinRoom(
                roomId = roomId,
                onSuccess = { ErrorHandler.showSuccess(requireContext(), "Joined room!") },
                onError = { msg -> ErrorHandler.showError(requireContext(), msg) }
            )
        }

        btnLeave.setOnClickListener {
            roomsRepo.leaveRoom(
                roomId = roomId,
                onSuccess = {
                    ErrorHandler.showSuccess(requireContext(), "Left room")
                    parentFragmentManager.popBackStack()
                },
                onError = { msg -> ErrorHandler.showError(requireContext(), msg) }
            )
        }
    }

    // sets up the room members adapter
    private fun setupMembersAdapter(myKey: String?) {
        adapter = RoomMembersAdapter(myKey)
        rvMembers.layoutManager = LinearLayoutManager(requireContext())
        rvMembers.adapter = adapter
    }


    // listens for changes to the user's current room status
    private fun listenToMyCurrentRoom(userKey: String?) {
        if (userKey.isNullOrBlank()) {
            btnLeave.visibility = View.GONE
            btnJoin.visibility = View.VISIBLE
            return
        }

        currentRoomRef = db.child("user_current_room").child(userKey)

        currentRoomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentRoomId = snapshot.getValue(String::class.java)
                isInThisRoom = (!currentRoomId.isNullOrBlank() && currentRoomId == roomId)

                if (isInThisRoom) {
                    btnLeave.visibility = View.VISIBLE
                    btnJoin.visibility = View.GONE
                } else {
                    btnLeave.visibility = View.GONE
                    btnJoin.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                ErrorHandler.showError(requireContext(), error.message, "Failed to load current room status")
            }
        }

        currentRoomRef!!.addValueEventListener(currentRoomListener!!)
    }

    // listens for real time updates to room details from firebase
    private fun listenRoomDetails() {
        val ref = db.child("rooms").child(roomId)

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("title").getValue(String::class.java) ?: "Room"
                val desc = snapshot.child("description").getValue(String::class.java) ?: ""

                val gName = snapshot.child("gameName").getValue(String::class.java) ?: ""
                val variant = snapshot.child("variant").getValue(String::class.java) ?: ""
                val partyType = snapshot.child("partyType").getValue(String::class.java) ?: ""

                val micRequired = snapshot.child("micRequired").getValue(Boolean::class.java) ?: false

                maxPlayers = (snapshot.child("maxPlayers").getValue(Long::class.java) ?: 0L).toInt()
                if (maxPlayers == 0) {
                    maxPlayers = when (partyType.lowercase()) {
                        "duo", "duos" -> 2
                        "trio", "trios" -> 3
                        "squad", "squads", "quads", "quad" -> 4
                        else -> 0
                    }
                }

                tvRoomName.text = name
                tvRoomHeader.text = name
                tvRoomDesc.text = if (desc.isBlank()) "No description" else desc

                val sub = listOf(gName, variant, partyType)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .uppercase()
                tvRoomSub.text = sub.ifBlank { "ROOM" }

                val purple = ContextCompat.getColor(requireContext(), R.color.purple_dark)
                val gray = ContextCompat.getColor(requireContext(), R.color.gray_light)

                if (micRequired) {
                    tvMicRequired.text = "Mic: Required"
                    tvMicRequired.setTextColor(purple)
                    ivMic.setColorFilter(purple)
                } else {
                    tvMicRequired.text = "Mic: Optional"
                    tvMicRequired.setTextColor(gray)
                    ivMic.setColorFilter(gray)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                ErrorHandler.showError(requireContext(), error.message, "Room read failed")
            }
        }

        ref.addValueEventListener(roomListener!!)
    }


    // listens for real time updates to room member list
    private fun listenRoomMembers() {
        val ref = db.child("room_members").child(roomId)

        membersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()

                tvPlayersCount.text =
                    if (maxPlayers > 0) "Players: $count/$maxPlayers" else "Players: $count/?"

                if (!snapshot.hasChildren()) {
                    adapter.submit(emptyList())
                    tvEmptyMembers.visibility = View.VISIBLE
                    return
                } else {
                    tvEmptyMembers.visibility = View.GONE
                }

                val list = mutableListOf<RoomMember>()
                for (child in snapshot.children) {
                    val userKey = child.key ?: continue
                    val nickname = child.child("nickname").getValue(String::class.java).orEmpty()
                    val username = child.child("username").getValue(String::class.java).orEmpty()
                    list.add(RoomMember(userKey = userKey, nickname = nickname, username = username))
                }

                val sorted = list.sortedWith(
                    compareByDescending<RoomMember> { it.userKey == myUserKey }
                        .thenBy { it.nickname.lowercase() }
                )

                adapter.submit(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                ErrorHandler.showError(requireContext(), error.message, "Failed to load members")
            }
        }

        ref.addValueEventListener(membersListener!!)
    }

    // removes all firebase listeners when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()

        roomListener?.let { db.child("rooms").child(roomId).removeEventListener(it) }
        membersListener?.let { db.child("room_members").child(roomId).removeEventListener(it) }

        currentRoomListener?.let { l ->
            currentRoomRef?.removeEventListener(l)
        }

        roomListener = null
        membersListener = null
        currentRoomListener = null
        currentRoomRef = null
    }
}
