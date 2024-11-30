package com.example.matchgame.api

import com.example.matchgame.LeaderboardEntry
import retrofit2.Call
import retrofit2.http.GET

interface LeaderboardApi {
    @GET("leaderboard")
    fun getLeaderboard(): Call<List<LeaderboardEntry>>
}

/* Code Attribution:
 * Title: Using Retrofit 2.x as REST Client - Tutorial
 * Authors: Lars Vogel, Simon Scholz, David Weiser
 * Date: 2024
 * Availability: https://www.vogella.com/tutorials/Retrofit/article.html (Accessed on 30 September 2024)
 */

/* Vogel, L, Scholz, S  Weiser, D. 2024. Using Retrofit 2.x as REST client - Tutorial
(Version 2.5)[Source Code] https://www.vogella.com/tutorials/Retrofit/article.html (Accessed on 30 September 2024)
* */