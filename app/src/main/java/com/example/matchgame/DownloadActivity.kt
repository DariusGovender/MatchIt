package com.example.matchgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.matchgame.models.BoardSize
import com.example.matchgame.models.UserImageList
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DownloadActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var etGameNameDownload: EditText
    private lateinit var btnDownloadGame: Button
    private var boardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_download)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clDownload)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etGameNameDownload = findViewById(R.id.etGameNameDownload)
        btnDownloadGame = findViewById(R.id.btnDownloadGame)

        btnDownloadGame.setOnClickListener{
            val gameName = etGameNameDownload.text.toString().trim()
            if (gameName.isBlank()) {
                Toast.makeText(this, getString(R.string.name_blank), Toast.LENGTH_LONG).show()
            }
            else {
                checkGameExist(gameName)
            }
        }

    }

    private fun checkGameExist(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val userImageList = document.toObject(UserImageList::class.java)

                if (userImageList != null && userImageList.images != null) {
                    val numCards = userImageList.images!!.size * 2  // Safely access the size of images
                    boardSize = BoardSize.getByValue(numCards)

                    // Game exists, now redirect to MainActivity with the required data
                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("EXTRA_BOARD_SIZE", boardSize)
                    intent.putExtra("EXTRA_GAME_NAME", customGameName)
                    startActivity(intent)
                    finish() // Close DownloadActivity after navigation
                } else {
                    // Handle case where userImageList or images is null
                    Toast.makeText(this, getString(R.string.game_not_found, customGameName), Toast.LENGTH_LONG).show()
                }
            } else {
                // Document does not exist
                Toast.makeText(this, getString(R.string.game_not_exist, customGameName), Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            // Handle any errors during Firestore operation
            Toast.makeText(this, getString(R.string.error_checking_game, exception.message), Toast.LENGTH_LONG).show()
        }
    }

}