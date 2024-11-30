package com.example.matchgame

// Data class that handles the individual leaderboard entries
data class LeaderboardEntry(
    val userId: String = "",
    val email: String = "",
    val score: Long = 0,
    val difficulty: String = ""
)