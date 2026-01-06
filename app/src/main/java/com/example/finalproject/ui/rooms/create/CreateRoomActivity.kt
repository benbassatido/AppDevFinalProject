package com.example.finalproject.ui.rooms.create

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R

class CreateRoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val modeTitle = intent.getStringExtra("modeTitle") ?: "Mode"

        findViewById<TextView>(R.id.tvCreateTitle).text = "Create Room • $modeTitle"

        val etName = findViewById<EditText>(R.id.etRoomName)

        findViewById<Button>(R.id.btnCreate).setOnClickListener {
            val roomName = etName.text.toString().trim()
            finish()
        }
    }
}
