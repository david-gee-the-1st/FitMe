package com.example.fitme

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Camera : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)

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
                    //No action needed already already at Camera
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
        bottomNavigation.selectedItemId = R.id.nav_camera

        // Automatically open the camera when activity loads
        openCamera()

        val btnOpenCamera = findViewById<Button?>(R.id.btnOpenCamera)
        btnOpenCamera?.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Check if a camera app is available
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Toast.makeText(this, "Picture captured successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
 */