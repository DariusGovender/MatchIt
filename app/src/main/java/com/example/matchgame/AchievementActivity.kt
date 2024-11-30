package com.example.matchgame

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchgame.models.Achievement
import com.example.matchgame.utils.isOnline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AchievementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AchievementAdapter
    private lateinit var dao: AchievementDao
    private lateinit var achievementRepos: AchievementRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.achievements_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get DAO from Room database
        dao = RoomDb.getDatabase(this).achieveDao()
        val firestore = Firebase.firestore
        achievementRepos = AchievementRepository.getInstance(dao, firestore)

        // Load achievements and set up RecyclerView

        lifecycleScope.launch {

            loadAchievements()
        }

    }

    // Syncs achievements with Firestore when the activity is resumed
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            achievementRepos.syncAchievementsToFirestore(userId)
            achievementRepos.syncAchievements(userId)

        }
    }


    // function to load achievements into recycler view, depending on network it will either load
    // from Room, or Firestore
    private suspend fun loadAchievements() {

        if (!this.isOnline()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val achievements = achievementRepos.getAllAchievements()
                Log.d(
                    "AchievementActivity",
                    "Loaded achievements: $achievements"
                )// Fetch all achievements from the database
                withContext(Dispatchers.Main) {
                    if (achievements.isNotEmpty()) {
                        adapter = AchievementAdapter(achievements, this@AchievementActivity)
                        recyclerView.adapter = adapter
                    } else {
                        Log.e("AchievementActivity", "No achievements found to display.")
                    }
                }
            }
        }
        else
        {       // If network connection, loads from Firestore
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            try {
                val achievements = mutableListOf<Achievement>()
                val firestore = Firebase.firestore
                // Fetch the document from Firestore for the given user ID
                val document = firestore.collection("achievements").document(userId).get().await()

                if (document.exists()) {
                    // Retrieve the document data

                    val achievementsData = document.data
                    if (!achievementsData.isNullOrEmpty()) {
                        achievementsData.forEach { (achievementKey, unlocked) ->
                            try {
                                val isUnlocked = unlocked as? Boolean ?: false
                                val achievement = Achievement(name = achievementKey, isUnlocked = isUnlocked)
                                achievements.add(achievement)
                                Log.d("Achievements", "Achievement: $achievementKey, Unlocked: $isUnlocked")
                                adapter = AchievementAdapter(achievements, this@AchievementActivity)
                                recyclerView.adapter = adapter
                                // If you want, you can add further processing here,
                                // such as displaying to UI or triggering some action based on the achievement
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
                Log.e("Achievements", "Error reading achievements: ${e.message}")
            }
        }
    }
}



