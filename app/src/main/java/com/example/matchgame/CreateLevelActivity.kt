package com.example.matchgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.matchgame.models.BoardSize

class CreateLevelActivity: AppCompatActivity() {

    companion object {
        private const val CREATE_REQUEST_CODE = 248
    }

    private lateinit var radioGroupSize: RadioGroup
    private lateinit var btnContinue: Button
    private var desiredBoardSize: BoardSize = BoardSize.EASY // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_level)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clCreateLevel)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        radioGroupSize = findViewById(R.id.radioGroupSize)
        btnContinue = findViewById(R.id.btnContinue)

        // Handle radio button selection
        radioGroupSize.setOnCheckedChangeListener { _, checkedId ->
            desiredBoardSize = when (checkedId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                R.id.rbHard -> BoardSize.HARD
                else -> BoardSize.EASY
            }
        }

        btnContinue.setOnClickListener {
            /*
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra("EXTRA_BOARD_SIZE", desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
            */
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra("EXTRA_BOARD_SIZE", desiredBoardSize)
            startActivity(intent)
        }
    }
}