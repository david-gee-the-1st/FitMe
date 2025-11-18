package com.example.fitme

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fitme.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: FitMeDatabase
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    // Biometric Variables:
    private var cancellationSignal: CancellationSignal? = null
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "The following error occurred: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Successful Login!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Login, Home::class.java)
                    startActivity(intent)
                }

        }

    private fun getCancellationSignal(): CancellationSignal{
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            Toast.makeText(applicationContext, "Authentication was cancelled by user", Toast.LENGTH_SHORT).show()
        }

        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean{
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure){
            Toast.makeText(applicationContext, "Please enable fingerprint authentication", Toast.LENGTH_SHORT).show()
            return false
        }

        if  (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(applicationContext, "Fingerprint permission is not enabled", Toast.LENGTH_SHORT).show()
            return false
        }
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            return true
        }
        else {
            return true
        }
    }



    // ViewModel initialization
    private val viewModel: LoginViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(userDao) as T
            }
        }
        ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.P)
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
        setupClickListeners()

        //Biometrics Login
        checkBiometricSupport()

        binding.btnBiometricScan.setOnClickListener {
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor, authenticationCallback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for FitMe")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)

        }

    }

    private fun setupEventListeners() {
        // Username and password text watchers
        binding.etUsername.onTextChanged {
            viewModel.onEvent(LoginEvent.checkUsername(it))
        }

        binding.etPassword.onTextChanged {
            viewModel.onEvent(LoginEvent.checkPassword(it))
        }

        // Navigate to Register Activity
        binding.btnRegisterNav.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            var uid : String = ""

            if (binding.etPassword.text.isNullOrEmpty()){
                Toast.makeText(this@Login, "Please input username/email", Toast.LENGTH_SHORT).show()
            }
            else if(binding.etUsername.text.isNullOrEmpty()){
                Toast.makeText(this@Login, "Please input password", Toast.LENGTH_SHORT).show()
            }
            else{
                val email = binding.etUsername.text.toString()
                val password = binding.etPassword.text.toString()
                val intent = Intent(this@Login, Home::class.java)

                lifecycleScope.launch {
                    //Online login:
                    val user = viewModel.login(email, password)

                    if (user != null) {
                        Toast.makeText(this@Login, "Login Successful", Toast.LENGTH_LONG).show()

                        //Toast.makeText(this@Login, user.email, Toast.LENGTH_LONG).show()

                        sessionManager.clearSession()

                        sessionManager.saveUserSession(
                            user.userId,
                            user.email,
                            user.username
                        )

                        startActivity(Intent(this@Login, Home::class.java))
                        finish()

                    } else {
                        Toast.makeText(this@Login, "Login Failed", Toast.LENGTH_LONG).show()
                        //Offline login:
                        sessionManager.clearSession()
                        viewModel.onEvent(LoginEvent.Login)
                    }
                }
            }
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

//Biometric scanner:


/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
To Do List App using Recycler View Android Studio Kotlin Example Tutorial. 2022. YouTube video, added by Code With Cal. [Online]. Available at: https://www.youtube.com/watch?v=RfIR4oaSVfQ [Accessed 20 September 2025].
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
 */