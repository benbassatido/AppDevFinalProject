package com.example.finalproject.ui.rooms

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.Room
import com.example.finalproject.ui.rooms.create.CreateRoomFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomsFragment : Fragment(R.layout.fragment_rooms) {

    private lateinit var adapter: RoomsAdapter
    private lateinit var btnBack: ImageButton

    private var listener: ValueEventListener? = null
    private var membersListener: ValueEventListener? = null
    private var currentRoomListener: ValueEventListener? = null

    private var gameId: String? = null
    private var gameName: String? = null
    private var variant: String? = null
    private var partyType: String? = null
    private var maxPlayers: Int = 0

    private var roomsQuery: Query? = null
    private var membersRef: DatabaseReference? = null
    private var currentRoomRef: DatabaseReference? = null

    private val latestRooms = mutableListOf<Room>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameId = arguments?.getString("gameId")
        gameName = arguments?.getString("gameName")
        variant = arguments?.getString("variant")
        partyType = arguments?.getString("partyType")
        maxPlayers = arguments?.getInt("maxPlayers", 0) ?: 0

        val rv = view.findViewById<RecyclerView>(R.id.rvRooms)
        val fab = view.findViewById<MaterialButton>(R.id.btnCreateRoom)
        btnBack = view.findViewById(R.id.btnBack)

        adapter = RoomsAdapter { room ->
            val f = RoomFragment().apply {
                arguments = bundleOf("roomId" to room.id)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .addToBackStack(null)
                .commit()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        fab.setOnClickListener {
            val f = CreateRoomFragment().apply {
                arguments = bundleOf(
                    "gameId" to gameId,
                    "gameName" to gameName,
                    "variant" to variant,
                    "partyType" to partyType,
                    "maxPlayers" to maxPlayers
                )
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, f)
                .addToBackStack(null)
                .commit()
        }

        startListenRooms()
        listenToMembersCounts()
        listenToCurrentRoom()
    }

    private fun startListenRooms() {
        roomsQuery?.let { q -> listener?.let { q.removeEventListener(it) } }

        roomsQuery =
            if (!gameId.isNullOrBlank())
                FirebaseDatabase.getInstance().getReference("rooms").orderByChild("gameId").equalTo(gameId)
            else
                FirebaseDatabase.getInstance().getReference("rooms")

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Room>()

                for (child in snapshot.children) {
                    val room = child.getValue(Room::class.java) ?: continue
                    room.id = child.key.orEmpty()

                    if (room.status != "open") continue

                    if (!variant.isNullOrBlank() && !room.variant.equals(variant!!, ignoreCase = true)) continue
                    if (!partyType.isNullOrBlank() && !room.partyType.equals(partyType!!, ignoreCase = true)) continue

                    list.add(room)
                }

                list.sortByDescending { it.createdAt }

                latestRooms.clear()
                latestRooms.addAll(list)
                adapter.submitList(latestRooms.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        }

        roomsQuery!!.addValueEventListener(listener!!)
    }

    private fun listenToMembersCounts() {
        membersRef = FirebaseDatabase.getInstance().getReference("room_members")

        membersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updated = latestRooms.map { room ->
                    val count = snapshot.child(room.id).childrenCount.toInt()

                    val max = when {
                        room.maxPlayers > 0 -> room.maxPlayers
                        maxPlayers > 0 -> maxPlayers
                        room.gameId == "cs2" -> 5
                        else -> partyTypeToMaxPlayers(room.partyType)
                    }

                    room.copy(currentPlayers = count, maxPlayers = max)
                }

                latestRooms.clear()
                latestRooms.addAll(updated)
                adapter.submitList(updated)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        membersRef!!.addValueEventListener(membersListener!!)
    }

    private fun partyTypeToMaxPlayers(partyType: String): Int {
        val t = partyType.lowercase()
        return when {
            t.contains("duo") -> 2
            t.contains("trio") -> 3
            t.contains("quad") || t.contains("squad") || t.contains("quads") || t.contains("squads") -> 4
            else -> 0
        }
    }

    private fun listenToCurrentRoom() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRepo = com.example.finalproject.data.repository.UsersRepository()

        usersRepo.ensureUserKey(
            uid = uid,
            onSuccess = { userKey ->
                currentRoomRef = FirebaseDatabase.getInstance()
                    .getReference("user_current_room")
                    .child(userKey)

                currentRoomListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentRoomId = snapshot.getValue(String::class.java)
                        adapter.setCurrentRoomId(currentRoomId)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }

                currentRoomRef!!.addValueEventListener(currentRoomListener!!)
            },
            onError = { adapter.setCurrentRoomId(null) }
        )
    }

    override fun onDestroyView() {
        roomsQuery?.let { q -> listener?.let { q.removeEventListener(it) } }
        membersRef?.let { ref -> membersListener?.let { ref.removeEventListener(it) } }
        currentRoomRef?.let { ref -> currentRoomListener?.let { ref.removeEventListener(it) } }

        roomsQuery = null
        membersRef = null
        currentRoomRef = null

        listener = null
        membersListener = null
        currentRoomListener = null

        super.onDestroyView()
    }
}
