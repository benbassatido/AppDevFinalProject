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
import com.example.finalproject.data.repository.RoomsRepository
import com.example.finalproject.ui.rooms.create.CreateRoomFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomsFragment : Fragment(R.layout.fragment_rooms) {

    private val repo = RoomsRepository()
    private lateinit var adapter: RoomsAdapter

    private lateinit var btnBack: ImageButton

    private var listener: ValueEventListener? = null
    private var membersListener: ValueEventListener? = null
    private var currentRoomListener: ValueEventListener? = null

    private var gameId: String? = null
    private var gameName: String? = null
    private var variant: String? = null
    private var partyType: String? = null

    private val latestRooms = mutableListOf<Room>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameId = arguments?.getString("gameId")
        gameName = arguments?.getString("gameName")
        variant = arguments?.getString("variant")
        partyType = arguments?.getString("partyType")


        val rv = view.findViewById<RecyclerView>(R.id.rvRooms)
        val fab = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreateRoom)
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
                    "partyType" to partyType
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
        listener = repo.listenToRooms(
            gameId = gameId,
            variant = variant,
            partyType = partyType,
            onResult = { list ->
                latestRooms.clear()
                latestRooms.addAll(list)
                adapter.submitList(latestRooms.toList())
            },
            onError = { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        )
    }

    private fun listenToMembersCounts() {
        val ref = FirebaseDatabase.getInstance().getReference("room_members")

        membersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updated = latestRooms.map { room ->
                    val count = snapshot.child(room.id).childrenCount.toInt()

                    val max = if (room.maxPlayers > 0) room.maxPlayers else when (room.partyType.lowercase()) {
                        "duo", "duos" -> 2
                        "trio", "trios" -> 3
                        "squad", "squads", "quads" -> 4
                        else -> 0
                    }

                    room.copy(currentPlayers = count, maxPlayers = max)
                }

                latestRooms.clear()
                latestRooms.addAll(updated)
                adapter.submitList(updated)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(membersListener!!)
    }

    private fun listenToCurrentRoom() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("user_current_room").child(uid)

        currentRoomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentRoomId = snapshot.getValue(String::class.java)
                adapter.setCurrentRoomId(currentRoomId)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(currentRoomListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        listener?.let {
            FirebaseDatabase.getInstance().getReference("rooms").removeEventListener(it)
        }
        membersListener?.let {
            FirebaseDatabase.getInstance().getReference("room_members").removeEventListener(it)
        }
        currentRoomListener?.let {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance().getReference("user_current_room").child(uid)
                    .removeEventListener(it)
            }
        }

        listener = null
        membersListener = null
        currentRoomListener = null
    }
}
