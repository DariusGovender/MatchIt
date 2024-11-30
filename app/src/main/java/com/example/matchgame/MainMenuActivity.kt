package com.example.matchgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import com.example.matchgame.utils.PermissionUtil

class MainMenuActivity : AppCompatActivity() {
    private lateinit var btnSinglePlayer : Button
    private lateinit var btnGameSharing : Button
    private lateinit var btnLeaderboard : Button
    private lateinit var imgbtnAchievements :ImageButton
    private lateinit var imgbtnHelp :ImageButton
    private lateinit var imgbtnSettings :ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_menu)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSinglePlayer = findViewById(R.id.btnSinglePlayer)
        btnGameSharing = findViewById(R.id.btnGameSharing)
        btnLeaderboard = findViewById(R.id.btnLeaderboard)

        imgbtnAchievements = findViewById(R.id.imgbtnAchievements)
        imgbtnHelp = findViewById(R.id.imgbtnHelp)
        imgbtnSettings = findViewById(R.id.imgbtnSettings)
        imgbtnHelp = findViewById(R.id.imgbtnHelp)


        if(!PermissionUtil.isNotificationPermissionGranted(this)) {
            PermissionUtil.showNotificationRationaleDialog(this) {
                PermissionUtil.openAppSettings(this)
            }
        }

        // Set click listeners
        btnSinglePlayer.setOnClickListener {
            val intent = Intent(this, SelectDifficultyActivity::class.java)
            startActivity(intent)
        }

        btnGameSharing.setOnClickListener {
            val intent = Intent(this, GameSharingActivity::class.java)
            startActivity(intent)
        }

        imgbtnHelp.setOnClickListener {
            // Handle Help button click
            // Navigate to Help Activity
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        btnLeaderboard.setOnClickListener {
            // Handle Leaderboard button click
            // Navigate to Leaderboard Activity or do any action
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }



        // Set click listeners for ImageButtons
        imgbtnAchievements.setOnClickListener {
            // Handle Achievements button click
            // Navigate to Achievements Activity
            val intent = Intent(this, AchievementActivity::class.java)
            startActivity(intent)
        }

        imgbtnHelp.setOnClickListener {
            // Handle Help button click
            // Navigate to Help Activity
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        imgbtnSettings.setOnClickListener {
            // Navigate to the Settings screen
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.exit_warning_title))
        builder.setMessage(getString(R.string.exit_warning_message))

        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            // If the user presses "Yes", exit
            super.onBackPressed()
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            // If the user presses "No", dismiss the dialog and remains on the MainMenuActivity
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}