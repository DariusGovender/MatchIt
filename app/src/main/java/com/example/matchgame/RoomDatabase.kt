package com.example.matchgame

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.matchgame.models.Achievement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Dao
interface AchievementDao {

    // database queries for achievements earned offline

    @Query("SELECT * FROM achievement_database")
    suspend fun getAllAchievements(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Query("DELETE FROM achievement_database")
    suspend fun deleteAchievements()

    @Query("DELETE FROM achievement_database")
    suspend fun clearDatabase()

}

// Room database creation
@Database(entities = [Achievement::class], version = 1, exportSchema = false)
abstract class RoomDb : RoomDatabase() {
    abstract fun achieveDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: RoomDb? = null

        fun getDatabase(context: Context): RoomDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDb::class.java,
                    "achievement_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


class AchievementRepository(
    private val dao: AchievementDao,
    private val firestore: FirebaseFirestore
) {

    // Will sync achievements to Room database when there is a connection
    suspend fun syncAchievements(userId: String) {
        try {
            val document = firestore.collection("achievements").document(userId).get().await()

            if (document.exists()) {
                val achievementsData = document.data
                if (!achievementsData.isNullOrEmpty()) {
                    achievementsData.forEach { (achievementKey, unlocked) ->
                        try {
                            val achievement = Achievement(
                                name = achievementKey,
                                isUnlocked = unlocked as Boolean
                            )
                            dao.insertAchievement(achievement)
                        } catch (e: ClassCastException) {
                            Log.e("Achievements", "Invalid data type for achievement $achievementKey: ${e.message}")
                        }
                    }
                } else {
                    Log.e("Achievements", "No achievements data found in the document for user $userId.")
                }
            } else {
                Log.e("Achievements", "No achievements found for user $userId.")
            }
        } catch (e: Exception) {
            Log.e("Achievements", "Error syncing achievements: ${e.message}")
        }
    }

    // Takes achievements earned offline and syncs it to firestore collection
    suspend fun syncAchievementsToFirestore(userId: String) {
        try {
            // Step 1: Retrieve all achievements from the local Room database
            val achievements = dao.getAllAchievements()

            if(achievements.size == 3) {

                // Step 2: Convert achievements to a Firestore-compatible format
                val achievementsMap = achievements.associate { achievement ->
                    achievement.name to achievement.isUnlocked
                }

                // Step 3: Update or set the document in Firestore
                firestore.collection("achievements").document(userId).set(achievementsMap)
                    .addOnSuccessListener {
                        Log.d(
                            "Achievements",
                            "Achievements successfully synced to Firestore for user $userId."
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "Achievements",
                            "Failed to sync achievements to Firestore for user $userId: ${e.message}"
                        )
                    }

            }
            else
            {
                return
            }
        } catch (e: Exception) {
            Log.e("Achievements", "Error syncing achievements to Firestore: ${e.message}")
        }
    }

    // Tracks an achievement earned and adds it to Room database
    suspend fun trackUnlock(achievement: Achievement) {
        dao.insertAchievement(achievement)
    }

    // Loads the achievements from Room database
    suspend fun getAllAchievements(): List<Achievement> {
        return dao.getAllAchievements()
    }


    companion object {
        @Volatile private var instance: AchievementRepository? = null

        fun getInstance(dao: AchievementDao, firestore: FirebaseFirestore): AchievementRepository {
            return instance ?: synchronized(this) {
                instance ?: AchievementRepository(dao, firestore).also { instance = it }
            }
        }
    }
}



