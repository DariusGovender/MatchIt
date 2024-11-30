//package com.example.matchgame.database
//
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import android.content.Context
//import com.example.matchgame.models.Achievement
//
//@Database(entities = [Achievement::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun achievementDao(): AchievementDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "achievement_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}
