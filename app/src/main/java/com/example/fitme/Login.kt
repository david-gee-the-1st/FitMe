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

//Auth0
import androidx.core.view.isVisible
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.google.android.material.snackbar.Snackbar

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: FitMeDatabase
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    //Auth0 authentication
    // Login/logout-related properties
    private lateinit var auth0: Auth0
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    //Auth0 video debug.
    private var userIsAuthenticated = false


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

        //Initialize SSO variables
        // Initialize Auth0
        auth0 = Auth0.getInstance(getString(R.string.com_auth0_client_id),getString(R.string.com_auth0_domain))

        binding.buttonLogin.setOnClickListener { login() }
        //binding.buttonLogout.setOnClickListener { logout() }

        // Initialize database and session
        database = FitMeDatabase.getDatabase(this)
        userDao = database.userDao()
        sessionManager = SessionManager(this)

        setupEventListeners()
        observeViewModel()

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

    //Auth0 login method
    private fun login() {
        WebAuthProvider
            .login(auth0)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope(getString(R.string.login_scopes))
            .withAudience(getString(R.string.login_audience, getString(R.string.com_auth0_domain)))
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(exception: AuthenticationException) {
                    showSnackBar(getString(R.string.login_failure_message, exception.getCode()))
                }

                override fun onSuccess(credentials: Credentials) {
                    // Keep credentials in memory
                    cachedCredentials = credentials
                    userIsAuthenticated = true
                    showSnackBar(getString(R.string.login_success_message, credentials.accessToken))

                    // Immediately fetch the user profile from Auth0 and then persist to local DB/session
                    fetchAuth0UserProfileAndPersist(credentials)
                }
            })
    }



    //Auth0 logout method
    private fun logout() {
        WebAuthProvider
            .logout(auth0)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Void?, AuthenticationException> {

                override fun onFailure(exception: AuthenticationException) {
                    updateUI()
                    showSnackBar(getString(R.string.general_failure_with_exception_code,
                        exception.getCode()))
                }

                override fun onSuccess(payload: Void?) {
                    cachedCredentials = null
                    cachedUserProfile = null
                    updateUI()
                }

            })
    }

    // --- New helper: fetch profile and persist locally ---
    private fun fetchAuth0UserProfileAndPersist(credentials: Credentials) {
        // Safety: require accessToken before calling userInfo
        val accessToken = credentials.accessToken
        if (accessToken == null) {
            showSnackBar("SSO login succeeded but no accessToken returned.")
            return
        }

        val authClient = AuthenticationAPIClient(auth0)
        authClient.userInfo(accessToken).start(object : Callback<UserProfile, AuthenticationException> {
            override fun onFailure(exception: AuthenticationException) {
                // Still proceed - but notify
                showSnackBar(getString(R.string.general_failure_with_exception_code, exception.getCode()))
                // fallback: save session with minimal info (no profile)
                sessionManager.saveUserSession("sso_user", "", "SSO User")
                navigateToHome()
            }

            override fun onSuccess(profile: UserProfile) {
                // Save profile in memory
                cachedUserProfile = profile

                // Determine best local values from profile
                val email = profile.email ?: ""
                val name = profile.name ?: profile.email ?: "SSO User"
                val localUserId = profile.email ?: "sso_user"   // uses email as ID


                // Save to SessionManager
                sessionManager.saveUserSession(localUserId, email, name)

                // Persist (upsert) a local User row so profile/editor work with a consistent local source
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        // Try to find an existing local user by email
                        var user = userDao.getUserByEmail(email)

                        if (user == null) {
                            // create a minimal user record for SSO users
                            user = User(
                                userId = localUserId,
                                username = name, // initially use name (or email)
                                email = email,
                                height = 0.0,
                                weight = 0.0,
                                dob = "",
                                phone = "",
                                password = "" // empty for SSO
                            )
                        } else {
                            // update username if it's empty and profile has a name
                            if (user.username.isBlank() && name.isNotBlank()) {
                                user = user.copy(username = name)
                            }
                        }

                        // Upsert user (uses Room Upsert in your UserDao)
                        userDao.upsertUser(user)

                        withContext(Dispatchers.Main) {
                            showSnackBar(getString(R.string.general_success_message))
                            // Now navigate to the home/profile flow
                            navigateToHome()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showSnackBar("Failed to persist SSO user locally: ${e.message}")
                            navigateToHome()
                        }
                    }
                }
            }
        })
    }

    // --- small helper to navigate to home (keeps existing behavior) ---
    private fun navigateToHome() {
        val intent = Intent(this@Login, Home::class.java)
        startActivity(intent)
        finish()
    }

    //Auth0 snackbar method
    private fun updateUI() {
        val isLoggedIn = cachedCredentials != null

//        binding.textviewTitle.text = if (isLoggedIn) {
//            getString(R.string.logged_in_title)
//        } else {
//            getString(R.string.logged_out_title)
//        }
//        binding.buttonLogin.isEnabled = !isLoggedIn
//        binding.buttonLogout.isEnabled = isLoggedIn
//        binding.linearlayoutMetadata.isVisible = isLoggedIn
//
//        binding.textviewUserProfile.isVisible = isLoggedIn
//
//        val userName = cachedUserProfile?.name ?: ""
//        val userEmail = cachedUserProfile?.email ?: ""
//        binding.textviewUserProfile.text = getString(R.string.user_profile, userName, userEmail)
//
//        if (!isLoggedIn) {
//            binding.edittextCountry.setText("")
//        }
    }

    private fun showSnackBar(text: String) {
        Snackbar
            .make(
                binding.root,
                text,
                Snackbar.LENGTH_LONG
            ).show()
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