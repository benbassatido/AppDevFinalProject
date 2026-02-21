package com.example.finalproject.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.firebase.FirebaseProvider
import com.example.finalproject.data.model.ChatMessage
import com.example.finalproject.data.repository.ChatRepository
import com.example.finalproject.data.repository.UsersRepository
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import com.example.finalproject.data.repository.RepositoryManager
import com.example.finalproject.ui.common.ErrorHandler

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val auth = FirebaseProvider.auth
    private val usersRepo = RepositoryManager.usersRepo
    private val chatRepo = RepositoryManager.chatRepo

    private val myUid: String by lazy {
        auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
    }
    private var myUserKey: String = ""

    private lateinit var tvChatTitle: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private lateinit var adapter: MessagesAdapter

    private var chatId: String = ""
    private var otherUserKey: String = ""
    private var otherNickname: String = ""
    private var listener: ValueEventListener? = null

    // extracts arguments passed to the fragment during creation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otherUserKey = requireArguments().getString("otherUserKey").orEmpty()
        otherNickname = requireArguments().getString("otherNickname").orEmpty()
    }

    // initializes the chat interface and sets up message sending functionality
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvChatTitle = view.findViewById(R.id.tvChatTitle)
        rvMessages = view.findViewById(R.id.rvMessages)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)

        tvChatTitle.text = otherNickname.ifBlank { "Chat" }

        val btnBack = view.findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }


        viewLifecycleOwner.lifecycleScope.launch {
            myUserKey = usersRepo.ensureUserKeySuspend(myUid)
            chatId = chatRepo.chatIdFor(myUserKey, otherUserKey)

            adapter = MessagesAdapter(myUserKey, otherNickname)
            rvMessages.layoutManager = LinearLayoutManager(requireContext())
            rvMessages.adapter = adapter

            chatRepo.ensureChat(chatId, myUserKey, otherUserKey)

            startListening()
        }

        btnSend.setOnClickListener {
            val text = etMessage.text?.toString().orEmpty()
            etMessage.setText("")
            viewLifecycleOwner.lifecycleScope.launch {
                chatRepo.sendMessage(chatId, text)
            }
        }
    }

    // starts listening for real time chat messages from firebase
    private fun startListening() {
        listener = chatRepo.listenMessages(
            chatId = chatId,
            limit = 100,
            onMessages = { list: List<ChatMessage> ->
                adapter.submit(list)
                if (list.isNotEmpty()) rvMessages.scrollToPosition(list.size - 1)
            },
            onError = { error: DatabaseError ->
                ErrorHandler.showError(requireContext(), error.message, "Failed to load messages")
            }
        )
    }

    // stops listening to chat messages when the view is destroyed
    override fun onDestroyView() {
        listener?.let { chatRepo.stopListening(chatId, it) }
        listener = null
        super.onDestroyView()
    }

    companion object {
        // creates a new ChatFragment instance with the specified parameters
        fun newInstance(otherUserKey: String, otherNickname: String): ChatFragment {
            val f = ChatFragment()
            f.arguments = Bundle().apply {
                putString("otherUserKey", otherUserKey)
                putString("otherNickname", otherNickname)
            }
            return f
        }
    }
}
