//package com.example.matchgame.database
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import androidx.room.Update
//import com.example.matchgame.models.Achievement
//
//@Dao
//interface AchievementDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAchievement(achievement: Achievement)
//
//    @Query("SELECT * FROM achievements WHERE name = :name")
//    suspend fun getAchievementByName(name: String): Achievement?
//
//    @Query("SELECT * FROM achievements")
//    suspend fun getAllAchievements(): List<Achievement>
//}