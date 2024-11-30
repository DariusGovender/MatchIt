package com.example.matchgame

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import java.util.concurrent.Executor
import android.provider.Settings

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE)

        // Load user preferences for theme
        val isDarkModeOn = sharedPreferences.getBoolean("NightMode", false)
        val savedLanguage = sharedPreferences.getString("AppLanguage", "English")
        changeAppLanguage(savedLanguage)

        // Set the theme based on user preference
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val isLoggedIn = sharedPreferences.getBoolean("IsLoggedIn", false)

            if (isLoggedIn) {
                // Check if biometric authentication is enabled
                val isBiometricEnabled = sharedPreferences.getBoolean("BiometricsEnabled", false)
                if (isBiometricEnabled) {
                    showBiometricPrompt()
                } else {
                    // If biometrics are not enabled, proceed to MainMenuActivity
                    navigateToMainMenu()
                }
            } else {
                // If not logged in, proceed to login activity after delay
                navigateToLogin()
            }
    }

    private fun changeAppLanguage(language: String?) {
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
    }

    private fun showBiometricPrompt() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // Check if the device has a secure lock screen (PIN, pattern, or password)
        if (!keyguardManager.isDeviceSecure) {
            // Show an alert dialog or a message prompting the user to enable device security
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.security_setup_required))
                .setMessage(getString(R.string.setup_screen_lock_message))
                .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                    // Open security settings for the user
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    // Close the app or take appropriate action if security is not enabled
                    finishAffinity()
                }
                .show()
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val intent = Intent(this@SplashActivity, MainMenuActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Handle errors, including the "Cancel" button click
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    // Close the app when "Cancel" is clicked
                    finishAffinity() // Close the app and remove it from the back stack
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setDeviceCredentialAllowed(true)
            .build()

        // Show the biometric prompt
        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToLogin() {
        // Show splash screen for 3 seconds, then move to the login activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the login activity
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)

            // Close this activity so it won't appear when the user presses back
            finish()
        }, 3000) // Delay for 3 seconds (3000 milliseconds)
    }

    private fun navigateToMainMenu() {
        // Start the MainMenuActivity
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}
