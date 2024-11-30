package com.example.matchgame

import android.content.Intent
import android.content.SharedPreferences
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
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var tvLogIn: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnGoogleLogin: Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private val TAG = "LoginActivity"
    private lateinit var sharedPreferences: SharedPreferences
    private val RC_SIGN_IN = 9001
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lcLogin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE)

        // Check if user is already logged in
        val isLoggedIn = sharedPreferences.getBoolean("IsLoggedIn", false)
        if (isLoggedIn) {
            // Navigate directly to MainMenuActivity
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()
        etEmail = findViewById(R.id.etEmail)
        loginButton = findViewById(R.id.btnLogin)
        etPassword = findViewById(R.id.etPassword)
        tvLogIn = findViewById(R.id.tvJoinNow)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("707355508111-k6ct31u1m3eiocuh6h09f5h8dl9tnbs1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        tvLogIn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null && !user.isEmailVerified) {
                                user.sendEmailVerification()
                                    .addOnCompleteListener { task -> }
                            } else if (user?.isEmailVerified == true) {
                                sharedPreferences.edit().putBoolean("IsLoggedIn", true).apply()

                                val intent = Intent(this, MainMenuActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.login_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { initializeUserAchievements(it.uid) }
                    val intent = Intent(this, MainMenuActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.login_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to initialize default achievements for the user in Firestore
    private fun initializeUserAchievements(userId: String) {
        val defaultAchievements = hashMapOf(
            "first_win" to false,
            "perfect_score" to false,
            "score_1000" to false,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: Exception) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, getString(R.string.google_sign_in_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
