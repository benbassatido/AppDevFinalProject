package com.example.finalproject.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.ChatMessage
import com.example.finalproject.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val repo = ChatRepository()
    private val myUid: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

    private lateinit var tvChatTitle: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private lateinit var adapter: MessagesAdapter

    private var chatId: String = ""
    private var otherUid: String = ""
    private var otherNickname: String = ""
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otherUid = requireArguments().getString("otherUid").orEmpty()
        otherNickname = requireArguments().getString("otherNickname").orEmpty()
        chatId = repo.chatIdFor(myUid, otherUid)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvChatTitle = view.findViewById(R.id.tvChatTitle)
        rvMessages = view.findViewById(R.id.rvMessages)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)

        tvChatTitle.text = otherNickname.ifBlank { "Chat" }

        adapter = MessagesAdapter(myUid, otherNickname)

        rvMessages.layoutManager = LinearLayoutManager(requireContext())
        rvMessages.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repo.ensureChat(chatId, myUid, otherUid)
        }

        btnSend.setOnClickListener {
            val text = etMessage.text?.toString().orEmpty()
            etMessage.setText("")
            viewLifecycleOwner.lifecycleScope.launch {
                repo.sendMessage(chatId, text)
            }
        }

        startListening()
    }

    private fun startListening() {
        listener = repo.listenMessages(
            chatId = chatId,
            limit = 100,
            onMessages = { list: List<ChatMessage> ->
                adapter.submit(list)
                if (list.isNotEmpty()) rvMessages.scrollToPosition(list.size - 1)
            },
            onError = { _: DatabaseError ->
            }
        )
    }

    override fun onDestroyView() {
        listener?.let { repo.stopListening(chatId, it) }
        listener = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(otherUid: String, otherNickname: String): ChatFragment {
            val f = ChatFragment()
            f.arguments = Bundle().apply {
                putString("otherUid", otherUid)
                putString("otherNickname", otherNickname)
            }
            return f
        }
    }
}
