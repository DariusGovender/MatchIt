package com.example.matchgame

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var rvLeaderBoard: RecyclerView
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var imgbtnRefreshLeaderbaord: ImageButton

    private lateinit var leaderboardEntries: MutableList<LeaderboardEntry>
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var selectedDifficulty = "EASY" // Default value for initial load

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.leaderboard)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        imgbtnRefreshLeaderbaord = findViewById(R.id.imgbtnRefreshLeaderbaord)

        val difficultyOptions = listOf("Default", "EASY", "MEDIUM", "HARD")

        // Setup spinners with options
        setupSpinners(difficultyOptions)

        rvLeaderBoard = findViewById(R.id.rvLeaderBoard)
        leaderboardEntries = mutableListOf()

        setupRecyclerView()

        // Firestore
        loadLeaderboardData()

        imgbtnRefreshLeaderbaord.setOnClickListener {
            spinnerDifficulty.setSelection(0)
            loadLeaderboardData() // Refresh leaderboard with filter
        }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter(leaderboardEntries)
        rvLeaderBoard.adapter = leaderboardAdapter
        rvLeaderBoard.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSpinners(difficultyOptions: List<String>) {
        // Setup Difficulty Spinner
        val difficultyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyOptions)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDifficulty.adapter = difficultyAdapter
        spinnerDifficulty.setSelection(0) // Default selection
        spinnerDifficulty.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                selectedDifficulty = difficultyOptions[position]
                loadLeaderboardDataFilter(selectedDifficulty)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }

    private fun loadLeaderboardDataFilter(diff: String) {
        leaderboardEntries.clear()
        val user = auth.currentUser
        val uID = user?.uid

        if (diff == "Default") {
            loadLeaderboardData()
            return
        }
        db.collection("leaderboard")
            .whereEqualTo("difficulty", diff)
            .whereEqualTo("type", "Default")
            .orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboardList = mutableListOf<LeaderboardEntry>()
                for (document in documents) {
                    val email = document.getString("email") ?: ""
                    val score = document.getLong("score") ?: 0
                    val difficulty = document.getString("difficulty") ?: ""
                    val entry = LeaderboardEntry(uID.toString(), email, score, difficulty)
                    leaderboardList.add(entry)
                }
                leaderboardAdapter.updateLeaderboardList(leaderboardList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting leaderboard data: ", exception)
            }
    }

    private fun loadLeaderboardData() {
        leaderboardEntries.clear()
        val user = auth.currentUser
        val uID = user?.uid

        db.collection("leaderboard")
            .whereEqualTo("type", "Default")
            .orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboardList = mutableListOf<LeaderboardEntry>()
                for (document in documents) {
                    val email = document.getString("email") ?: ""
                    val score = document.getLong("score") ?: 0
                    val difficulty = document.getString("difficulty") ?: ""
                    val entry = LeaderboardEntry(uID.toString(), email, score, difficulty)
                    leaderboardList.add(entry)
                }
                leaderboardAdapter.updateLeaderboardList(leaderboardList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting leaderboard data: ", exception)
            }
    }
}
