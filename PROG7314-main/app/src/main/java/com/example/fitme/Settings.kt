package com.example.fitme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBackProfile : Button = findViewById(R.id.btnBackProfile)
        val btnEditProfile: Button = findViewById(R.id.btnEditProfile)
        val btnChangePassword: Button = findViewById(R.id.btnChangePassword)
        val btnAboutUs: Button = findViewById(R.id.btnAboutUs)
        val btnPrivacyPolicy: Button = findViewById(R.id.btnPrivacyPolicy)
        val btnTerms: Button = findViewById(R.id.btnTerms)

        btnBackProfile.setOnClickListener()
        {
            val intent : Intent = Intent (this, Profile::class.java)
            startActivity(intent)
        }


        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEditor::class.java))
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, PasswordChange::class.java))
        }

        btnAboutUs.setOnClickListener {
            startActivity(Intent(this, AboutUs::class.java))
        }

        btnPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicy::class.java))
        }

        btnTerms.setOnClickListener {
            startActivity(Intent(this, TermsAndConditions::class.java))
        }
    }
}