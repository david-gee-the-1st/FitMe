package com.example.fitme

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fitme.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: FitMeDatabase
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    // ViewModel initialization
    private val viewModel: LoginViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(userDao) as T
            }
        }
        ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and session
        database = FitMeDatabase.getDatabase(this)
        userDao = database.userDao()
        sessionManager = SessionManager(this)

        setupEventListeners()
        observeViewModel()
    }

    private fun setupEventListeners() {
        // Username and password text watchers
        binding.etUsername.onTextChanged {
            viewModel.onEvent(LoginEvent.checkUsername(it))
        }

        binding.etPassword.onTextChanged {
            viewModel.onEvent(LoginEvent.checkPassword(it))
        }

        // Login button
        binding.btnLogin.setOnClickListener {
            sessionManager.clearSession()
            viewModel.onEvent(LoginEvent.Login)
        }

        // Navigate to Register Activity
        binding.btnRegisterNav.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                // Show any error message
                state.errorMessage?.let {
                    Toast.makeText(this@Login, it, Toast.LENGTH_SHORT).show()
                }

                // If login is successful
                if (state.isSuccess) {
                    lifecycleScope.launch {
                        try {
                            val username = state.username
                            val user = withContext(Dispatchers.IO) {
                                var user = userDao.getUserByUsername(username)
                                if (user == null && Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                                    user = userDao.getUserByEmail(username)
                                }
                                user
                            }

                            if (user != null) {
                                // Save session
                                sessionManager.saveUserSession(
                                    user.userId,
                                    user.email,
                                    user.username
                                )

                                Toast.makeText(
                                    this@Login,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Navigate to Home Activity
                                val intent = Intent(this@Login, Home::class.java)
                                startActivity(intent)
                                finish() // optional: close login screen
                            } else {
                                Toast.makeText(
                                    this@Login,
                                    "User not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@Login,
                                "Error retrieving user data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Extension to simplify EditText text change handling
     */
    fun android.widget.EditText.onTextChanged(listener: (String) -> Unit) {
        this.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                listener(s.toString())
            }
        })
    }
}

/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
To Do List App using Recycler View Android Studio Kotlin Example Tutorial. 2022. YouTube video, added by Code With Cal. [Online]. Available at: https://www.youtube.com/watch?v=RfIR4oaSVfQ [Accessed 20 September 2025].
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
 */