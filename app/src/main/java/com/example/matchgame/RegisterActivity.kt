package com.example.matchgame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var tvJoinNow: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var etConfirmPassword: EditText

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clRegister)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvJoinNow = findViewById(R.id.tvJoinNow)
        tvJoinNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password.length >= 6) {
                    if (password == confirmPassword) {
                        registerUser(email, password)
                    } else {
                        Toast.makeText(this, getString(R.string.password_mismatch_error), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.password_length_error), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.fill_fields_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(this, getString(R.string.verification_email_sent, email), Toast.LENGTH_SHORT).show()
                            Log.d("Register", "Verification email sent.")
                        } else {
                            Toast.makeText(this, getString(R.string.registration_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                            Log.e("Register", "Failed to send verification email: ${verificationTask.exception?.message}")
                        }
                    }

                    user?.let { initializeUserAchievements(it.uid) }

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("Register", "Registration failed: ${task.exception?.message}")
                    Toast.makeText(this,  getString(R.string.registration_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to initialize default achievements for the user in Firestore
    private fun initializeUserAchievements(userId: String) {
        val defaultAchievements = hashMapOf(
            "First Win" to false,
            "Perfect Score" to false,
            "1000 Score" to false,
            // Add any additional achievements here
        )

        db.collection("achievements").document(userId)
            .set(defaultAchievements)
            .addOnSuccessListener {
                Log.d("Achievements", "Default achievements initialized for user $userId.")
            }
            .addOnFailureListener { e ->
                Log.e("Achievements", "Error initializing achievements: ${e.message}")
            }
    }
}
