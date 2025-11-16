package com.example.fitme

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Settings : AppCompatActivity() {

    private val NOTIF_PERMISSION = 1001
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var switchNotifications: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize notification helper
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        // Initialize switch
        switchNotifications = findViewById(R.id.switchNotifications)

        // Load saved switch state
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedState = prefs.getBoolean("notifications_enabled", false)
        switchNotifications.isChecked = savedState

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState(isChecked)

            if (isChecked) {
                // Request notification permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIF_PERMISSION)
                } else {
                    notificationHelper.scheduleDailyNotification()
                    Toast.makeText(this, "Daily reminders ON", Toast.LENGTH_SHORT).show()
                }
            } else {
                notificationHelper.cancelDailyNotification()
                Toast.makeText(this, "Daily reminders OFF", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigation Buttons
        val btnBackProfile: Button = findViewById(R.id.btnBackProfile)
        val btnEditProfile: Button = findViewById(R.id.btnEditProfile)
        val btnChangePassword: Button = findViewById(R.id.btnChangePassword)
        val btnAboutUs: Button = findViewById(R.id.btnAboutUs)
        val btnPrivacyPolicy: Button = findViewById(R.id.btnPrivacyPolicy)
        val btnTerms: Button = findViewById(R.id.btnTerms)

        btnBackProfile.setOnClickListener { startActivity(Intent(this, Profile::class.java)) }
        btnEditProfile.setOnClickListener { startActivity(Intent(this, ProfileEditor::class.java)) }
        btnChangePassword.setOnClickListener { startActivity(Intent(this, PasswordChange::class.java)) }
        btnAboutUs.setOnClickListener { startActivity(Intent(this, AboutUs::class.java)) }
        btnPrivacyPolicy.setOnClickListener { startActivity(Intent(this, PrivacyPolicy::class.java)) }
        btnTerms.setOnClickListener { startActivity(Intent(this, TermsAndConditions::class.java)) }
    }

    private fun saveSwitchState(state: Boolean) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled", state).apply()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIF_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            notificationHelper.scheduleDailyNotification()
            Toast.makeText(this, "Daily reminders ON", Toast.LENGTH_SHORT).show()
        } else {
            switchNotifications.isChecked = false
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
