package com.example.matchgame.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_database")
data class Achievement(

    @PrimaryKey val name: String,  // Name of the achievement
    val isUnlocked: Boolean  // Whether the achievement is unlocked or locked
)

