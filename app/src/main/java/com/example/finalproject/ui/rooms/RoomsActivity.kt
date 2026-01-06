package com.example.finalproject.ui.rooms

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.ui.rooms.create.CreateRoomActivity

class RoomsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rooms)

        val gameId = intent.getStringExtra("gameId") ?: return
        val modeId = intent.getStringExtra("modeId") ?: return
        val modeTitle = intent.getStringExtra("modeTitle") ?: modeId.uppercase()

        findViewById<TextView>(R.id.tvRoomsTitle).text = "Rooms • $modeTitle"

        val rv = findViewById<RecyclerView>(R.id.rvRooms)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        val adapter = RoomsAdapter(emptyList()) { roomName ->
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val roomsForThisMode: List<String> = emptyList()

        tvEmpty.visibility = if (roomsForThisMode.isEmpty()) View.VISIBLE else View.GONE
        adapter.update(roomsForThisMode)

        findViewById<Button>(R.id.btnCreateRoom).setOnClickListener {
            val i = Intent(this, CreateRoomActivity::class.java)
            i.putExtra("gameId", gameId)
            i.putExtra("modeId", modeId)
            i.putExtra("modeTitle", modeTitle)
            startActivity(i)
        }
    }
}
