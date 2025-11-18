package com.example.fitme

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FoodSyncRepo(private val foodIntakeDao: FoodIntakeDao,
                   private val foodIntakeFirebaseRepo: FoodIntakeFirebaseRepository
) {

    suspend fun syncFromApiToLocal(apiFoods: List<AuthResponse>) {
        // Convert API foods to Room entities
        val roomFoods = apiFoods.map { apiFood ->
            FoodIntake(
                id = 0, // Auto-generate
                foodName = apiFood.name ?: "",
                calories = apiFood.calories?.toInt() ?: 0,
                date = apiFood.createdAt?.substring(0, 10) ?: getCurrentDate() // Extract date part
            )
        }

        // Save to Room
        roomFoods.forEach { food ->
            foodIntakeDao.addIntake(food)
        }

        // Sync to Firebase
        roomFoods.forEach { food ->
            foodIntakeFirebaseRepo.pushFoodIntakeToFirebase(food)
        }
    }

    suspend fun syncLocalToApi(food: FoodIntake): AuthResponse {
        // Convert Room entity to API model
        val apiFood = AuthResponse(
            _id = null,
            name = food.foodName,
            calories = food.calories.toDouble(),
            userID = "current-user", // You'll need to get actual user ID
            createdAt = "${food.date}T00:00:00.000Z",
            updatedAt = "${food.date}T00:00:00.000Z"
        )

        // Return the API response (you'll handle the actual API call elsewhere)
        return apiFood
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}