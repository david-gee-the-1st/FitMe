package com.example.fitme

import android.util.Log

data class UserState(
    val username: String = "",
    val email: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val dob: String = "",
    val phone: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    fun isValid(): Boolean {
        if (username.isBlank()) {
            Log.d("Logging", "Username is required")
            return false
        }

        if (password.isBlank()) {
            Log.d("Logging", "Password is required")
            return false
        }

        if (email.isBlank()) {
            Log.d("Logging", "Email is required")
            return false
        }
        if (height <= 0.0) {
            Log.d("Logging", "Valid height is required")
            return false
        }

        if (weight <= 0.0) {
            Log.d("Logging", "Valid weight is required")
            return false
        }
        if (dob.isBlank()) {
            Log.d("Logging", "Date of birth is required")
            return false
        }

        return if (email.contains("@")) {
            username.isNotBlank() && password.isNotBlank() && email.isNotBlank() && dob.isNotBlank()
        } else {
            Log.d("Logging", "email does not have @")
            false
        }
    }
}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */
