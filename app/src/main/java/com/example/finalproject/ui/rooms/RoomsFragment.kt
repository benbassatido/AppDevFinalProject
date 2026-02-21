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
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.Room
import com.example.finalproject.data.repository.GameOptionsRepository
import com.example.finalproject.ui.rooms.create.CreateRoomFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler

class RoomsFragment : Fragment(R.layout.fragment_rooms) {

    private val auth = FirebaseProvider.auth
    private val database = FirebaseProvider.database
    private val usersRepo = RepositoryManager.usersRepo

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

    // initializes the rooms list screen with filters and listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameId = arguments?.getString("gameId")
        gameName = arguments?.getString("gameName")
        variant = arguments?.getString("variant")
        partyType = arguments?.getString("partyType")
        maxPlayers = arguments?.getInt("maxPlayers", 0) ?: 0

        val rv = view.findViewById<RecyclerView>(R.id.rvRooms)
        val fab = view.findViewById<FloatingActionButton>(R.id.btnCreateRoom)
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

    // starts listening for real time room updates from firebase
    private fun startListenRooms() {
        roomsQuery?.let { q -> listener?.let { q.removeEventListener(it) } }

        roomsQuery =
            if (!gameId.isNullOrBlank())
                database.getReference("rooms").orderByChild("gameId").equalTo(gameId)
            else
                database.getReference("rooms")

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Room>()

                for (child in snapshot.children) {
                    val room = child.getValue(Room::class.java)?.copy(id = child.key.orEmpty()) ?: continue

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
                ErrorHandler.showError(requireContext(), error.message, "Failed to load rooms")
            }
        }

        roomsQuery!!.addValueEventListener(listener!!)
    }

    // listens for real time updates to room member counts
    private fun listenToMembersCounts() {
        membersRef = database.getReference("room_members")

        membersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updated = latestRooms.map { room ->
                    val count = snapshot.child(room.id).childrenCount.toInt()

                    val max = when {
                        room.maxPlayers > 0 -> room.maxPlayers
                        maxPlayers > 0 -> maxPlayers
                        room.gameId == "cs2" -> 5
                        else -> GameOptionsRepository.maxPlayersForPartyType(room.partyType)
                    }

                    room.copy(currentPlayers = count, maxPlayers = max)
                }

                latestRooms.clear()
                latestRooms.addAll(updated)
                adapter.submitList(updated)
            }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Failed to load member counts", Toast.LENGTH_SHORT).show()
                    }
        }

        membersRef!!.addValueEventListener(membersListener!!)
    }


    // listens for updates to the current user's room status
    private fun listenToCurrentRoom() {
        val uid = auth.currentUser?.uid ?: return

        usersRepo.ensureUserKey(
            uid = uid,
            onSuccess = { userKey ->
                currentRoomRef = database.getReference("user_current_room").child(userKey)

                currentRoomListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentRoomId = snapshot.getValue(String::class.java)
                        adapter.setCurrentRoomId(currentRoomId)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Failed to load current room status", Toast.LENGTH_SHORT).show()
                    }
                }

                currentRoomRef!!.addValueEventListener(currentRoomListener!!)
            },
            onError = { adapter.setCurrentRoomId(null) }
        )
    }

    // removes all firebase listeners when the view is destroyed
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
