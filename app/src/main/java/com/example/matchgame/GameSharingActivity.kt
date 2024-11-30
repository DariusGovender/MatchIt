package com.example.matchgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GameSharingActivity : AppCompatActivity() {
    private lateinit var btnCreate: Button
    private lateinit var btnDownload: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_sharing)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clGamesharing)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnCreate = findViewById(R.id.btnCreate)
        btnDownload = findViewById(R.id.btnDownload)

        btnCreate.setOnClickListener {
            val intent = Intent(this, CreateLevelActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnDownload.setOnClickListener {
            val intent = Intent(this, DownloadActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}