package com.example.matchgame

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchgame.models.BoardSize
import com.example.matchgame.models.MemoryGame
import com.google.android.material.snackbar.Snackbar
import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.matchgame.models.Achievement
import com.github.jinatonic.confetti.CommonConfetti
import com.example.matchgame.models.UserImageList
import com.example.matchgame.utils.EXTRA_BOARD_SIZE
import com.example.matchgame.utils.EXTRA_GAME_NAME
import com.example.matchgame.utils.isOnline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 248
    }

    private lateinit var main: ConstraintLayout
    private lateinit var rvBoard : RecyclerView
    private lateinit var tvNumMove: TextView
    private lateinit var tvNumPairs: TextView
    private var startTime: Long = 0
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var boardSize: BoardSize
    private lateinit var auth: FirebaseAuth
    private lateinit var achievementRepos: AchievementRepository
    private val dao: AchievementDao by lazy {
        RoomDb.getDatabase(this).achieveDao()
    }

    private val db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null
    private lateinit var downloadGameName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        auth = FirebaseAuth.getInstance()
        achievementRepos = AchievementRepository.getInstance(dao, db)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        downloadGameName = intent.getSerializableExtra(EXTRA_GAME_NAME) as String

        if (downloadGameName != "Default") {
            downloadGame(downloadGameName)
        }

        rvBoard = findViewById(R.id.rvBoard)
        tvNumMove = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        main = findViewById(R.id.main)

        setupBoard()
    }

    //override Methods
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if (customGameName == null) {
                Log.e(TAG, "Got null custom game from CreateActivity")
                return
            }
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if (userImageList?.images == null) {
                Log.e(TAG, "Invalid custom game data from Firebase")
                Toast.makeText(this, getString(R.string.not_found, customGameName), Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            customGameImages = userImageList.images
            gameName = customGameName
            setupBoard()
        }
    }

    //private methods
    private fun updateGameWithFlip(position: Int) {
        // Error checking
        if (memoryGame.haveWonGame()) {
            Snackbar.make(main, getString(R.string.game_already_won), Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            Snackbar.make(main, getString(R.string.invalid_move), Snackbar.LENGTH_SHORT).show()
            return
        }

        if (memoryGame.flipCard(position)) {
            Log.i(TAG, "Found a match! Num pairs found ${memoryGame.numPairsFound}")
            updateLeaderBoard()
        }

        tvNumMove.text = getString(R.string.moves_label, memoryGame.getNumMoves())
        adapter.notifyDataSetChanged()
    }

    private fun updateLeaderBoard()
    {
        // Colour progression based on pairs found
        val colour = ArgbEvaluator().evaluate(
            memoryGame.numPairsFound.toFloat() / boardSize.getNumOfPairs(),
            ContextCompat.getColor(this, R.color.color_progress_none),
            ContextCompat.getColor(this, R.color.color_progress_full)
        ) as Int
        tvNumPairs.setTextColor(colour)
        tvNumPairs.text = getString(R.string.pairs_label, memoryGame.numPairsFound, boardSize.getNumOfPairs())

        if (memoryGame.haveWonGame()) {
            unlockAchievement("First Win")
            CommonConfetti.rainingConfetti(main, intArrayOf(Color.YELLOW, Color.GREEN, Color.MAGENTA)).oneShot()
            val endTime = System.currentTimeMillis()
            val totalTime = (endTime - startTime) / 1000.0

            val moves = memoryGame.getNumMoves()
            val score = calculateScore(moves, totalTime)

            val user = auth.currentUser
            val uID = user?.uid
            val email = user?.email

            val leaderboard = db.collection("leaderboard")

            if (uID != null && email != null) {
                // Checks for existing user score
                leaderboard
                    .whereEqualTo("email", email)
                    .whereEqualTo("difficulty", boardSize.name)
                    .whereEqualTo("type", downloadGameName)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // Creates a new entry if it doesn't exist
                            val scoreData = hashMapOf(
                                "email" to email,
                                "score" to score,
                                "difficulty" to boardSize.name,
                                "type" to downloadGameName
                            )
                            leaderboard.document("$uID${System.currentTimeMillis()}").set(scoreData)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Score successfully written")
                                    Toast.makeText(this, getString(R.string.new_high_score), Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error writing score", e)
                                }
                        } else {
                            // Checks for highest score
                            for (document in documents) {
                                val existingScore = document.getLong("score") ?: 0
                                if (score > existingScore) {
                                    // Updates High Score
                                    leaderboard.document(document.id)
                                        .update("score", score)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Score successfully updated")
                                            Toast.makeText(this, getString(R.string.new_high_score), Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "Error updating score", e)
                                        }
                                }
                                else
                                {
                                    Toast.makeText(this, getString(R.string.win_message), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error retrieving documents", e)
                    }
            }


            Handler(Looper.getMainLooper()).postDelayed({
                // Adds delay to show toast message before moving to GameOverActivity
                val intent = Intent(this, GameoverActivity::class.java)
                intent.putExtra("EXTRA_SCORE", score)
                intent.putExtra("EXTRA_BOARD_SIZE", boardSize)
                intent.putExtra("EXTRA_GAME_NAME", downloadGameName)
                startActivity(intent)
                finish()
            }, 2000)
            return
        }
    }
        // Method to calculate score based on moves and time

    private fun calculateScore(moves: Int, totalTime: Double): Int {
        val baseScore = 1000

        val timeBonusMultiplier = when (boardSize) {
            BoardSize.EASY -> 1.0
            BoardSize.MEDIUM -> 1.5
            BoardSize.HARD -> 2.0
        }
        val timeBonus = (1000 / totalTime * timeBonusMultiplier).toInt()

        val minimumMovesToWin = when (boardSize) {
            BoardSize.EASY -> 4
            BoardSize.MEDIUM -> 9
            BoardSize.HARD -> 12
        }

        val movePenalty = when (boardSize) {
            BoardSize.EASY -> (moves - minimumMovesToWin) * 5
            BoardSize.MEDIUM -> (moves - minimumMovesToWin) * 5
            BoardSize.HARD -> (moves - minimumMovesToWin) * 5
        }.coerceAtLeast(0)

        // Calculate final score
        val finalScore = baseScore + timeBonus - movePenalty

        // Check for "Perfect Score" Achievement
        checkForPerfectScoreAchievement(finalScore)

        checkIfScoreIsGreaterThan1000(finalScore)

        return finalScore
    }

    private fun checkIfScoreIsGreaterThan1000(finalScore: Int){
        if(finalScore > 1000) {
            unlockAchievement("1000 Score")
        }
    }

    private fun checkForPerfectScoreAchievement(finalScore: Int) {
        val perfectScoreThreshold = when (boardSize) {
            BoardSize.EASY -> 1200
            BoardSize.MEDIUM -> 1500
            BoardSize.HARD -> 1800
        }

        // If the final score meets or exceeds the threshold, unlock the achievement
        if (finalScore >= perfectScoreThreshold) {
            unlockAchievement("Perfect Score")
        }
    }

    // Function to unlock an achievement if it's not already unlocked
    private fun unlockAchievement(achievementKey: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("Achievements", "User is not authenticated.")
            return
        }

        if(!this.isOnline())
        {
            lifecycleScope.launch {
                val achievement = Achievement(name = achievementKey, isUnlocked = true)
                lifecycleScope.launch {
                    achievementRepos.trackUnlock(achievement)
                }
                Log.d("Achievements", "$achievementKey  hawesss unlocked for user $userId.")

            }
            return
        }

        // Check if the achievement is already unlocked
        db.collection("achievements").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val isUnlocked = document.getBoolean(achievementKey) ?: false

                    // If the achievement is not unlocked, update it
                    if (!isUnlocked) {
                        db.collection("achievements").document(userId)
                            .update(achievementKey, true)
                            .addOnSuccessListener {
                                Log.d("Achievements", "$achievementKey unlocked for user $userId.")
                                Toast.makeText(this, getString(R.string.achievement_unlocked), Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Achievements", "Error unlocking achievement: ${e.message}")
                            }
                    } else {
                        Log.d("Achievements", "$achievementKey already unlocked for user $userId.")
                    }
                } else {
                    Log.e("Achievements", "No achievements found for user $userId.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Achievements", "Error retrieving achievements: ${e.message}")
            }
    }

    // method to handle board setup based on level
    private fun setupBoard() {
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumMove.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMove.text = "Medium: 6 x 3"
                tvNumPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMove.text = "Easy: 6 x 4"
                tvNumPairs.text = "Pairs: 0 / 12"
            }
        }

        startTime = System.currentTimeMillis()
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize, customGameImages)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardCLickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }
        })

        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }
}