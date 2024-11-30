package com.example.matchgame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.RadioGroup
import com.example.matchgame.models.BoardSize

class SelectDifficultyActivity : AppCompatActivity() {

    private lateinit var radioGroupSize: RadioGroup
    private lateinit var btnStartGame: Button
    private var selectedBoardSize: BoardSize = BoardSize.EASY // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_difficulty)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clSelectLevel)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

            radioGroupSize = findViewById(R.id.radioGroupSize)
            btnStartGame = findViewById(R.id.btnStartGame)

            // Handle radio button selection
            radioGroupSize.setOnCheckedChangeListener { _, checkedId ->
                selectedBoardSize = when (checkedId) {
                    R.id.rbEasy -> BoardSize.EASY
                    R.id.rbMedium -> BoardSize.MEDIUM
                    R.id.rbHard -> BoardSize.HARD
                    else -> BoardSize.EASY
                }
            }

            // Start game with selected level
            btnStartGame.setOnClickListener {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("EXTRA_BOARD_SIZE", selectedBoardSize)
                intent.putExtra("EXTRA_GAME_NAME", "Default")
                startActivity(intent)
                finish()
            }
    }
}

