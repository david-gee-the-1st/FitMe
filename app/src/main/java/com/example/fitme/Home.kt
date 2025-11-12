package com.example.fitme

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Bottom Navigation View:
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    //No action needed already already at Home
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
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_home
    }
}
/*
Reference list:
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
 */