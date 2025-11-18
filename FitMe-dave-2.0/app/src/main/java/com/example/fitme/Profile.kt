package com.example.fitme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // existing navigation setup...
        val btnSettingsNav : ImageButton = findViewById(R.id.btnSettingsNav)
        btnSettingsNav.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

        // Bind the TextViews
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)

        //Dynamic calorie tracker
        val tvCalorieProgress = findViewById<TextView>(R.id.tvCaloriesValue)
        val progressCalories = findViewById<ProgressBar>(R.id.progressCalories)

        // Initialize DB and session manager
        val database = FitMeDatabase.getDatabase(this)
        val userDao = database.userDao()
        val foodDao = database.foodIntakeDao()
        val sessionManager = SessionManager(this)

        // Load the currently logged in user from SessionManager
        val userId = sessionManager.getUserId()
        val userEmailFromSession = sessionManager.getUserEmail()

        // --- DYNAMIC CALORIES SECTION (READ FROM FOODINTAKEDAO) ---
        lifecycleScope.launch(Dispatchers.IO) {
            // Prefer lookup by userId, but fall back to email
            var user: User? = null
            if (!userId.isNullOrBlank()) {
                user = userDao.getUserById(userId)
            }
            if (user == null && !userEmailFromSession.isNullOrBlank()) {
                user = userDao.getUserByEmail(userEmailFromSession)
            }

            // Always fetch calories from FoodIntakeDao as the single source of truth (Progress page)
            val today = LocalDate.now().toString()
            val todaysCalories = foodDao.getTotalCalories(today) ?: 0

            // Sync caloriesToday field with DB user record
            val updatedUser =
                if (user != null) {
                    val newUser = user.copy(caloriesToday = todaysCalories)
                    userDao.upsertUser(newUser)
                    newUser
                } else null


            withContext(Dispatchers.Main) {
                if (user != null) {
                    // Username: if SSO user has no username → show email
                    tvUsername.text = user.username.ifBlank { user.email }
                    tvEmail.text = user.email

                    // Calorie summary should reflect the Progress page
                    val limit = user.calorieLimit
                    val consumed = user.caloriesToday

                    tvCalorieProgress.text = "$consumed / $limit kcal"
                    progressCalories.max = limit
                    progressCalories.progress = consumed
                } else {
                    // No local user (shouldn't happen after SSO fix) — fallback to session values
                    tvUsername.text = userEmailFromSession ?: "Guest"
                    tvEmail.text = userEmailFromSession ?: ""
                }
            }
        }

        //Navigate to Settings:
        btnSettingsNav.setOnClickListener()
        {
            val intent : Intent = Intent (this, Settings::class.java)
            startActivity(intent)
        }

        //Bottom Navigation View:
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_progress -> {
                    val intent = Intent(this, Progress::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_camera -> {
                    val intent = Intent(this, Camera::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this, AddIntake::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    //No action needed already already at Profile
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_profile

    }

}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
 */