package com.example.fitme

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PasswordChange : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password_change)

        // Initialize views
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        toolbar = findViewById(R.id.changePasswordToolbar)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Back navigation to Settings
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, Settings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // Change password button
        btnChangePassword.setOnClickListener {
            val current = etCurrentPassword.text.toString()
            val new = etNewPassword.text.toString()
            val confirm = etConfirmPassword.text.toString()

            if (validateInputs(current, new, confirm)) {
                // TODO: Implement actual password update logic here
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()

                // Clear fields after successful change
                etCurrentPassword.text.clear()
                etNewPassword.text.clear()
                etConfirmPassword.text.clear()
            }
        }
    }

    private fun validateInputs(current: String, new: String, confirm: String): Boolean {
        if (TextUtils.isEmpty(current)) {
            etCurrentPassword.error = "Current password is required"
            return false
        }
        if (TextUtils.isEmpty(new)) {
            etNewPassword.error = "New password is required"
            return false
        }
        if (new.length < 6) {
            etNewPassword.error = "Password must be at least 6 characters"
            return false
        }
        if (new != confirm) {
            etConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        // Navigate back to Settings when toolbar back icon is pressed
        val intent = Intent(this, Settings::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        return true
    }
}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */