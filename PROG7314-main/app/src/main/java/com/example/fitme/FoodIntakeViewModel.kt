package com.example.fitme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class FoodIntakeViewModel : ViewModel() {
    private val db = Firebase.firestore

    // Function to add a food intake document
    suspend fun addFoodIntake(food: AuthResponse): Boolean {
        return try {
            val db = Firebase.firestore
            val foodCollection = db.collection("FoodIntake")

            // Let Firestore generate document ID automatically
            foodCollection.add(food).await()

            Log.d("Firestore", "Food intake added successfully")
            true
        } catch (e: Exception) {
            Log.e("Firestore Error", "Failed to add food intake: ${e.message}")
            false
        }
    }

    // Function to retrieve all food intakes for a specific user
    suspend fun getFoodIntakeByUser(userID: String): List<AuthResponse> {
        return try {
            val db = Firebase.firestore
            val result = db.collection("FoodIntake")
                .whereEqualTo("userID", userID)
                .get()
                .await()

            result.toObjects(AuthResponse::class.java)
        } catch (e: Exception) {
            Log.e("Firestore Error", "Failed to fetch food intake: ${e.message}")
            emptyList()
        }
    }
}

/*
Reference list:
Firebase. 2025. Authenticate with Firebase using Password-Based Accounts on Android. [Online]. Available at: https://firebase.google.com/docs/auth/android/password-auth [Accessed 22 May 2025].
Mohsen Mashkour, 2023. How to send data to the Firebase Realtime database. Android studio Kotlin. [video online]. Available at: https://www.youtube.com/watch?v=3XiZF1UBn50&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ&index=2&ab_channel=MohsenMashkour [Accessed 22 May 2025].
*/
