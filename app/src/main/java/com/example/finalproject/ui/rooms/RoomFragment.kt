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
import com.example.finalproject.data.repository.RoomsRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomFragment : Fragment(R.layout.fragment_room) {

    private val db: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { RoomsRepository() }

    private var roomId: String = ""

    private var roomListener: ValueEventListener? = null
    private var membersListener: ValueEventListener? = null
    private var currentRoomListener: ValueEventListener? = null

    // views
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomId = requireArguments().getString("roomId").orEmpty()
        if (roomId.isBlank()) {
            Toast.makeText(requireContext(), "Missing roomId", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

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

        adapter = RoomMembersAdapter(auth.currentUser?.uid)
        rvMembers.layoutManager = LinearLayoutManager(requireContext())
        rvMembers.adapter = adapter

        btnJoin.text = "JOIN ROOM"

        btnJoin.setOnClickListener {
            repo.joinRoom(
                roomId = roomId,
                onSuccess = { Toast.makeText(requireContext(), "Joined room!", Toast.LENGTH_SHORT).show() },
                onError = { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show() }
            )
        }

        btnLeave.setOnClickListener {
            repo.leaveRoom(
                roomId = roomId,
                onSuccess = { Toast.makeText(requireContext(), "Left room", Toast.LENGTH_SHORT).show() },
                onError = { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show() }
            )
        }

        listenRoomDetails()
        listenRoomMembers()
        listenToMyCurrentRoom()
    }

    private fun listenToMyCurrentRoom() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.child("user_current_room").child(uid)

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

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(currentRoomListener!!)
    }

    private fun listenRoomDetails() {
        val ref = db.child("rooms").child(roomId)

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("title").getValue(String::class.java)
                    ?: snapshot.child("roomName").getValue(String::class.java)
                    ?: "Room"

                val desc = snapshot.child("description").getValue(String::class.java) ?: ""

                // ✅ תואם למודל שלך (RoomsRepository/Room.kt)
                val gName = snapshot.child("gameName").getValue(String::class.java) ?: ""
                val variant = snapshot.child("variant").getValue(String::class.java) ?: ""
                val partyType = snapshot.child("partyType").getValue(String::class.java) ?: ""

                val micRequired = snapshot.child("micRequired").getValue(Boolean::class.java) ?: false

                maxPlayers = snapshot.child("maxPlayers").getValue(Int::class.java)
                    ?: run {
                        when (partyType.lowercase()) {
                            "duo", "duos" -> 2
                            "trio", "trios" -> 3
                            "squad", "squads", "quads", "quad" -> 4
                            else -> 0
                        }
                    }

                tvRoomName.text = name
                tvRoomDesc.text = if (desc.isBlank()) "No description" else desc

                val sub = listOf(gName, variant, partyType)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .uppercase()
                tvRoomSub.text = if (sub.isBlank()) "ROOM" else sub

                val purple = 0xFF7C3AED.toInt()

                if (micRequired) {
                    tvMicRequired.text = "Mic: Required"
                    tvMicRequired.setTextColor(purple)
                    ivMic.setColorFilter(purple)
                } else {
                    tvMicRequired.text = "Mic: Optional"
                    tvMicRequired.setTextColor(0xFFB9C2CF.toInt())
                    ivMic.setColorFilter(0xFFB9C2CF.toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Room read failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        ref.addValueEventListener(roomListener!!)
    }

    private fun listenRoomMembers() {
        val ref = db.child("room_members").child(roomId)

        membersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uids = snapshot.children.mapNotNull { it.key }
                val count = uids.size

                tvPlayersCount.text =
                    if (maxPlayers > 0) "Players: $count/$maxPlayers" else "Players: $count/?"

                if (uids.isEmpty()) {
                    adapter.submit(emptyList())
                    tvEmptyMembers.visibility = View.VISIBLE
                    return
                } else {
                    tvEmptyMembers.visibility = View.GONE
                }

                fetchMembersUsers(uids)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(membersListener!!)
    }

    private fun fetchMembersUsers(uids: List<String>) {
        val usersRef = db.child("users")
        val result = mutableListOf<RoomMemberUi>()
        var remaining = uids.size

        uids.forEach { uid ->
            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val nickname = s.child("nickname").getValue(String::class.java) ?: ""
                    val username = s.child("username").getValue(String::class.java) ?: ""
                    result.add(RoomMemberUi(uid, nickname, username))
                    remaining--
                    if (remaining == 0) {
                        val myUid = auth.currentUser?.uid
                        val sorted = result.sortedWith(
                            compareByDescending<RoomMemberUi> { it.uid == myUid }.thenBy { it.nickname }
                        )
                        adapter.submit(sorted)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    remaining--
                    if (remaining == 0) adapter.submit(result)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        roomListener?.let { db.child("rooms").child(roomId).removeEventListener(it) }
        membersListener?.let { db.child("room_members").child(roomId).removeEventListener(it) }
        currentRoomListener?.let {
            val uid = auth.currentUser?.uid
            if (uid != null) db.child("user_current_room").child(uid).removeEventListener(it)
        }
    }
}
