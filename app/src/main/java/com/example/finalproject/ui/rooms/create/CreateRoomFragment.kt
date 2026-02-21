package com.example.finalproject.ui.rooms.create

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.finalproject.R
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.Room
import com.example.finalproject.data.repository.RoomsRepository
import com.example.finalproject.data.repository.UsersRepository
import com.example.finalproject.ui.rooms.RoomFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler

class CreateRoomFragment : Fragment(R.layout.fragment_create_room) {

    private val auth = FirebaseProvider.auth
    private val database = FirebaseProvider.database
    private val roomsRepo = RepositoryManager.roomsRepo
    private val usersRepo = RepositoryManager.usersRepo

    // initializes the create room form and handles room creation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val etTitle = view.findViewById<TextInputEditText>(R.id.etRoomTitle)
        val etDesc = view.findViewById<TextInputEditText>(R.id.etDescription)
        val swMic = view.findViewById<SwitchMaterial>(R.id.swMicRequired)
        val btnCreate = view.findViewById<MaterialButton>(R.id.btnCreateRoom)

        val gameId = arguments?.getString("gameId") ?: ""
        val gameName = arguments?.getString("gameName") ?: ""
        val variant = arguments?.getString("variant") ?: ""
        val partyType = arguments?.getString("partyType") ?: ""
        val maxPlayers = arguments?.getInt("maxPlayers", 0) ?: 0

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val uid = auth.currentUser?.uid.orEmpty()

        btnCreate.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val description = etDesc.text?.toString()?.trim().orEmpty()
            val micRequired = swMic.isChecked

            if (uid.isBlank()) {
                Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (title.isBlank()) {
                Toast.makeText(requireContext(), "Please enter room name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (maxPlayers <= 0) {
                Toast.makeText(requireContext(), "Invalid max players", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCreate.isEnabled = false
            btnCreate.text = "Creating..."

            usersRepo.ensureUserKey(
                uid = uid,
                onSuccess = { userKey ->
                    database.reference.child("users").child(userKey).child("nickname").get()
                        .addOnSuccessListener { snap ->
                            val nickname = snap.getValue(String::class.java) ?: "Owner"

                            val room = Room(
                                title = title,
                                description = description,
                                micRequired = micRequired,
                                gameId = gameId,
                                gameName = gameName,
                                variant = variant,
                                partyType = partyType,
                                maxPlayers = maxPlayers,
                                ownerName = nickname
                            )

                            roomsRepo.createRoom(
                                room = room,
                                onSuccess = { roomId ->
                                    roomsRepo.joinRoom(
                                        roomId = roomId,
                                        onSuccess = {
                                            btnCreate.isEnabled = true
                                            btnCreate.text = "CREATE ROOM"

                                            parentFragmentManager.popBackStack()

                                            val next = RoomFragment().apply {
                                                arguments = bundleOf("roomId" to roomId)
                                            }
                                            parentFragmentManager.beginTransaction()
                                                .replace(R.id.fragmentContainer, next)
                                                .addToBackStack(null)
                                                .commit()
                                        },
                                        onError = { msg ->
                                            btnCreate.isEnabled = true
                                            btnCreate.text = "CREATE ROOM"
                                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                onError = { msg ->
                                    btnCreate.isEnabled = true
                                    btnCreate.text = "CREATE ROOM"
                                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        .addOnFailureListener {
                            btnCreate.isEnabled = true
                            btnCreate.text = "CREATE ROOM"
                            Toast.makeText(requireContext(), "Failed to read nickname", Toast.LENGTH_SHORT).show()
                        }
                },
                onError = { msg ->
                    btnCreate.isEnabled = true
                    btnCreate.text = "CREATE ROOM"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
