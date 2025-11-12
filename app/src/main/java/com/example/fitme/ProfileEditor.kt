package com.example.fitme

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileEditor : AppCompatActivity() {

    private lateinit var database: FitMeDatabase
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_editor)

        val toolbar = findViewById<Toolbar>(R.id.editProfileToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // init
        database = FitMeDatabase.getDatabase(this)
        userDao = database.userDao()
        sessionManager = SessionManager(this)

        // Views
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Load current user
        val userId = sessionManager.getUserId()
        val sessionEmail = sessionManager.getUserEmail()
        lifecycleScope.launch(Dispatchers.IO) {
            var user: User? = null
            if (!userId.isNullOrBlank()) {
                user = userDao.getUserById(userId)
            }
            if (user == null && !sessionEmail.isNullOrBlank()) {
                user = userDao.getUserByEmail(sessionEmail)
            }
            withContext(Dispatchers.Main) {
                if (user != null) {
                    etUsername.setText(user.username)
                    etEmail.setText(user.email)
                    etHeight.setText(if (user.height == 0.0) "" else user.height.toString())
                    etWeight.setText(if (user.weight == 0.0) "" else user.weight.toString())
                    etPhone.setText(user.phone)
                }
            }
        }

        // Save changes
        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val height = etHeight.text.toString().toDoubleOrNull() ?: 0.0
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
            val phone = etPhone.text.toString().trim()

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Look up by session id/email to preserve same userId if exists
                    val existingUserId = sessionManager.getUserId()
                    var user: User? = null
                    if (!existingUserId.isNullOrBlank()) {
                        user = userDao.getUserById(existingUserId)
                    }
                    if (user == null && email.isNotBlank()) {
                        user = userDao.getUserByEmail(email)
                    }

                    if (user == null) {
                        // Create a new user row if none found
                        val newUser = User(
                            username = username.ifBlank { email },
                            email = email,
                            height = height,
                            weight = weight,
                            dob = "",
                            phone = phone,
                            password = ""
                        )
                        userDao.upsertUser(newUser)
                        sessionManager.saveUserSession(newUser.userId, newUser.email, newUser.username)
                    } else {
                        // Update existing
                        val updated = user.copy(
                            username = username.ifBlank { email },
                            email = email,
                            height = height,
                            weight = weight,
                            phone = phone
                        )
                        userDao.upsertUser(updated)
                        sessionManager.saveUserSession(updated.userId, updated.email, updated.username)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileEditor, "Profile updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileEditor, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */
