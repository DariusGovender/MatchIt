package com.example.matchgame

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var btnLang: Button
    private lateinit var btnLogOut: Button
    private lateinit var swDarkMode: Switch
    private lateinit var swNotifications: Switch
    private lateinit var swBiometrics: Switch
    private lateinit var emailTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clSettings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE)

        btnLang = findViewById(R.id.btnLang)

        btnLang.setOnClickListener {
           showLanguageSelection()
        }

        // Get current user email
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        btnLogOut = findViewById(R.id.btnLogOut)
        swBiometrics = findViewById(R.id.swBiometric)
        swDarkMode = findViewById(R.id.swDarkMode)
        swNotifications = findViewById(R.id.swNotifications)
        emailTextView = findViewById(R.id.emailTextView)

        emailTextView.text = email

        // Load the saved preferences and update the switches' states
        swDarkMode.isChecked = sharedPreferences.getBoolean("NightMode", false)
        swBiometrics.isChecked = sharedPreferences.getBoolean("BiometricsEnabled", false)
        swNotifications.isChecked = sharedPreferences.getBoolean("NotificationsEnabled", true)

        btnLogOut.setOnClickListener{
            signOut()
        }

        // Handle dark mode switch
        swDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // Disable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            // Save the preference
            sharedPreferences.edit().putBoolean("NightMode", isChecked).apply()
        }

        // Handle biometric switch
        swBiometrics.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, getString(R.string.biometrics_enabled), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.biometrics_disabled), Toast.LENGTH_SHORT).show()
            }
            // Save the biometric preference
            sharedPreferences.edit().putBoolean("BiometricsEnabled", isChecked).apply()
        }

        // Handle notifications switch
        swNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show()
            }

            // Save the notification preference
            sharedPreferences.edit().putBoolean("NotificationsEnabled", isChecked).apply()
        }
    }

    private fun showLanguageSelection() {

        // Creates an inflator and inflates the layout
        val inflaterView = LayoutInflater.from(this).inflate(R.layout.language_selection, null)
        val inflation = AlertDialog.Builder(this).setView(inflaterView).create()

        // Initialize the spinner and set the adapter
        val spinner = inflaterView.findViewById<Spinner>(R.id.spinnerLanguage)
        val languages = arrayOf("English", "Afrikaans", "Zulu", "Spanish", "Chinese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set the selected item based on the saved language preference
        val savedLanguage = sharedPreferences.getString("AppLanguage", "English")
        val pos = languages.indexOf(savedLanguage)
        spinner.setSelection(if (pos >= 0) pos else 0)

        val btnSave = inflaterView.findViewById<Button>(R.id.btnSave)

        // Set the background color of the dialog to transparent
        inflation.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        inflation.show()

        btnSave.setOnClickListener {
            val selectedLanguage = languages[spinner.selectedItemPosition]
            changeLanguage(selectedLanguage)
            inflation.dismiss()
        }
    }

    // Changes language locale and updates share preferences
    private fun changeLanguage(language: String) {
        val locale = when (language) {
            "Afrikaans" -> Locale("af")
            "Zulu" -> Locale("zu")
            "Spanish" -> Locale("es")
            "Chinese" -> Locale("zh")
            else -> Locale("en")
        }

        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        sharedPreferences.edit().putString("AppLanguage", language).apply()

        recreate()
    }

    private fun signOut() {
        // Clear login state
        sharedPreferences.edit().putBoolean("IsLoggedIn", false).apply()



        lifecycleScope.launch {
            // Access the DAO and clear the database
            val achievementDao = RoomDb.getDatabase(applicationContext).achieveDao()
            achievementDao.clearDatabase()

            Log.d("SettingsActivity", "Database cleared successfully")
        }

        // Navigate back to the LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

/* Code Attribution:
 * Title: Settings Activity with Language, Dark Mode, and Biometric Settings
 * Author: ChatGPT
 * Availability: https://chatgpt.com/?model=auto
 * Date Accessed: 2024-11-04
 */