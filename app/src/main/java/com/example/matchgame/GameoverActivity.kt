package com.example.matchgame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.matchgame.models.BoardSize
import com.example.matchgame.utils.EXTRA_BOARD_SIZE
import com.example.matchgame.utils.EXTRA_GAME_NAME
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GameoverActivity : AppCompatActivity() {

    private lateinit var progressBarDoubleScore: ProgressBar
    private lateinit var imgbtnHome: ImageButton
    private lateinit var imgbtnRestart: ImageButton
    private lateinit var btnDoubleScore: Button
    private lateinit var tvHighScore: TextView

    private lateinit var downloadGameName: String
    private lateinit var boardSize: BoardSize

    private var rewardedAd: RewardedAd? = null
    private var score: Int = 0

    private lateinit var auth: FirebaseAuth

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gameover)

        MobileAds.initialize(this)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameover)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        downloadGameName = intent.getSerializableExtra(EXTRA_GAME_NAME) as String
        score = intent.getIntExtra("EXTRA_SCORE", 0)

        tvHighScore = findViewById(R.id.tvHighScore)
        imgbtnHome = findViewById(R.id.imgbtnHome)
        imgbtnRestart = findViewById(R.id.imgbtnRestart)
        btnDoubleScore = findViewById(R.id.btnDoubleScore)
        progressBarDoubleScore = findViewById(R.id.progressBarDoubleScore)

        tvHighScore.text = getString(R.string.high_score_label) + " $score"

        loadRewardedAd()

        // Set up click listeners for buttons
        imgbtnHome.setOnClickListener {
            navigateToHome()
        }

        btnDoubleScore.setOnClickListener {
            // displays ad and double score if ad watched
            showRewardedAd()
        }

        imgbtnRestart.setOnClickListener {
            restartGame()
        }
    }

    private fun navigateToHome() {
        // Intent to go back to home activity
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish() // Close the GameOverActivity
    }

    private fun restartGame() {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("EXTRA_BOARD_SIZE", boardSize)
        intent.putExtra("EXTRA_GAME_NAME", downloadGameName)
        startActivity(intent)
        finish()
    }

    private fun loadRewardedAd() {
        btnDoubleScore.isEnabled = false // Disable the button while loading the ad
        progressBarDoubleScore.visibility = View.VISIBLE // Show the ProgressBar

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                progressBarDoubleScore.visibility = View.GONE // Hide the ProgressBar
                btnDoubleScore.isEnabled = true // Enable the button
                Toast.makeText(this@GameoverActivity, getString(R.string.ad_loaded_message), Toast.LENGTH_SHORT).show()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                btnDoubleScore.isEnabled = false // Disable the button while loading the ad
                progressBarDoubleScore.visibility = View.VISIBLE // Show the ProgressBar
                Toast.makeText(this@GameoverActivity, getString(R.string.ad_load_failed_message, adError.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showRewardedAd() {
        rewardedAd?.let { ad ->
            ad.show(this) { rewardItem ->
                // Double the score once the user earns the reward
                doubleScore()
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.ad_not_ready_message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun doubleScore() {
        score *= 2
        tvHighScore.text = "Score: $score"
        Toast.makeText(this, getString(R.string.score_doubled), Toast.LENGTH_SHORT).show()

        // Update the leaderboard with the new score
        updateLeaderBoard()
    }

    private fun updateLeaderBoard() {
        // Assuming you have user authentication and Firestore setup
        val user = auth.currentUser
        val uID = user?.uid
        val email = user?.email

        val leaderboard = db.collection("leaderboard")

        if (uID != null && email != null) {
            leaderboard
                .whereEqualTo("email", email)
                .whereEqualTo("difficulty", boardSize.name)
                .whereEqualTo("type", downloadGameName)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Create a new entry if it doesn't exist
                        val scoreData = hashMapOf(
                            "email" to email,
                            "score" to score,
                            "difficulty" to boardSize.name,
                            "type" to downloadGameName
                        )
                        leaderboard.document("$uID${System.currentTimeMillis()}").set(scoreData)
                            .addOnSuccessListener {
                                Toast.makeText(this, getString(R.string.new_high_score_message), Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->

                            }
                    } else {
                        // Check for highest score
                        for (document in documents) {
                            val existingScore = document.getLong("score") ?: 0
                            if (score > existingScore) {
                                // Update High Score
                                leaderboard.document(document.id)
                                    .update("score", score)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, getString(R.string.new_high_score_message), Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                    }
                            } else {
                                Toast.makeText(this, getString(R.string.you_won_message), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                }
        }
    }
}

