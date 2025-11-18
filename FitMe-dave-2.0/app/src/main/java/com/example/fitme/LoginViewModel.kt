package com.example.fitme

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginViewModel(private val dao: UserDao) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()
    private var _loggedInUser: User? = null

    private val userFirebaseRepo = UserFirebaseRepository()

    fun hasPass(hashPassword : String): String
    {
        val bytes = hashPassword.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        return digest.fold("") { str, byte -> str + "%02x".format(byte) }
    }
    fun verifyPassword(providedPassword: String, storedHash: String): Boolean {
        val providedPasswordHash = hasPass(providedPassword)
        return providedPasswordHash == storedHash
    }
    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.checkUsername -> {
                _loginState.update { it.copy(username = event.username, errorMessage = null) }
            }
            is LoginEvent.checkPassword -> {
                _loginState.update { it.copy(password = event.password, errorMessage = null) }
            }
            LoginEvent.Login -> {
                attemptLogin()
            }
        }
    }

    private fun attemptLogin() {
        val state = loginState.value
        Log.d("LoginViewModel", "Attempting login for user: ${state.username}")

        if (state.username.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Username is required") }
            Log.d("LoginViewModel", "Username is blank.")
            return
        }

        if (state.password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Password is required") }
            Log.d("LoginViewModel", "Password is blank.")
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                var user = dao.getUserByUsername(state.username)

                if (user == null && Patterns.EMAIL_ADDRESS.matcher(state.username).matches()) {
                    user = dao.getUserByEmail(state.username)
                    Log.d("LoginViewModel", "Initial username lookup failed. Attempting lookup by email.")
                }

                if (user == null) {
                    Log.d("LoginViewModel", "User not found in local DB. Checking Firebase...")
                    user = checkFirebaseForUser(state.username, state.password)
                }

                if (user == null) {
                    Log.d("LoginViewModel", "User lookup failed. User not found.")
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Invalid username or password"
                        )
                    }
                    return@launch
                }

                Log.d("LoginViewModel", "User lookup successful. User found: ${user.email}")

                if (!verifyPassword(state.password, user.password)) {
                    Log.d("LoginViewModel", "Password mismatch.")
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Invalid username or password"
                        )
                    }
                    return@launch
                }

                Log.d("LoginViewModel", "Password match successful. Login complete.")
                _loggedInUser = user
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login attempt failed with exception: ${e.message}")
                _loginState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Login failed: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun checkFirebaseForUser(username: String, password: String): User? {
        return try {
            Log.d("LoginViewModel", "Searching for user in Firebase by email: $username")

            val result = userFirebaseRepo.getUserByEmail(username)
            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    Log.d("LoginViewModel", "Found user in Firebase: ${firebaseUser.email}")

                    val providedPasswordHash = hasPass(password)
                    if (firebaseUser.password == providedPasswordHash) {
                        Log.d("LoginViewModel", "Firebase password verification successful")

                        val localUser = User(
                            userId = firebaseUser.userId,
                            username = firebaseUser.username,
                            email = firebaseUser.email,
                            height = firebaseUser.height,
                            weight = firebaseUser.weight,
                            dob = firebaseUser.dob,
                            phone = firebaseUser.phone,
                            password = firebaseUser.password,
                            calorieLimit = firebaseUser.calorieLimit,
                            caloriesToday = firebaseUser.caloriesToday
                        )

                        dao.upsertUser(localUser)
                        Log.d("LoginViewModel", "User synced from Firebase to local database")
                        return localUser
                    } else {
                        Log.d("LoginViewModel", "Firebase password verification failed")
                    }
                } else {
                    Log.d("LoginViewModel", "User not found in Firebase")
                }
            } else {
                Log.e("LoginViewModel", "Firebase query failed: ${result.exceptionOrNull()?.message}")
            }
            null
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error checking Firebase: ${e.message}")
            null
        }
    }

    class LoginViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */