package com.example.fitme

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserFirebaseRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val usersReference: DatabaseReference = database.child("users")

    suspend fun pushUserToFirebase(user: User): Result<String> {
        return try {
            usersReference.child(user.userId).setValue(user).await()
            Result.success(user.userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserInFirebase(user: User): Result<Unit> {
        return try {
            usersReference.child(user.userId).setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserFromFirebase(userId: String): Result<Unit> {
        return try {
            usersReference.child(userId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFromFirebase(userId: String): Result<User?> {
        return try {
            val snapshot = usersReference.child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val snapshot = usersReference
                .orderByChild("email")
                .equalTo(email)
                .get()
                .await()

            val user = snapshot.children.firstOrNull()?.getValue(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserCalories(userId: String, caloriesToday: Int): Result<Unit> {
        return try {
            usersReference.child(userId).child("caloriesToday").setValue(caloriesToday).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserCalorieLimit(userId: String, calorieLimit: Int): Result<Unit> {
        return try {
            usersReference.child(userId).child("calorieLimit").setValue(calorieLimit).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}