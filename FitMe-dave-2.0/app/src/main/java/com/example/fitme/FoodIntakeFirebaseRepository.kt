package com.example.fitme

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FoodIntakeFirebaseRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val foodIntakeReference: DatabaseReference = database.child("food_intakes")

    suspend fun pushFoodIntakeToFirebase(foodIntake: FoodIntake): Result<String> {
        return try {
            val foodKey = foodIntakeReference.push().key
                ?: return Result.failure(Exception("Failed to generate key"))

            val foodWithFirebaseId = foodIntake.copy(id = foodKey.hashCode())
            foodIntakeReference.child(foodKey).setValue(foodWithFirebaseId).await()
            Result.success(foodKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFoodIntakesByDate(date: String): Result<List<FoodIntake>> {
        return try {
            val snapshot = foodIntakeReference
                .orderByChild("date")
                .equalTo(date)
                .get()
                .await()

            val foodIntakes = snapshot.children.mapNotNull { child ->
                child.getValue(FoodIntake::class.java)
            }
            Result.success(foodIntakes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeeklyCalories(startDate: String, endDate: String): Result<List<DailyCalories>> {
        return try {
            val snapshot = foodIntakeReference
                .orderByChild("date")
                .startAt(startDate)
                .endAt(endDate)
                .get()
                .await()

            val dailyTotals = snapshot.children
                .mapNotNull { it.getValue(FoodIntake::class.java) }
                .groupBy { it.date }
                .map { (date, intakes) ->
                    DailyCalories(date, intakes.sumOf { it.calories })
                }

            Result.success(dailyTotals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFoodIntakeFromFirebase(foodId: Int): Result<Unit> {
        return try {
            val snapshot = foodIntakeReference
                .orderByChild("id")
                .equalTo(foodId.toDouble())
                .get()
                .await()

            snapshot.children.firstOrNull()?.ref?.removeValue()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFoodIntakeInFirebase(foodIntake: FoodIntake): Result<Unit> {
        return try {
            val snapshot = foodIntakeReference
                .orderByChild("id")
                .equalTo(foodIntake.id.toDouble())
                .get()
                .await()

            val updateResult = snapshot.children.firstOrNull()?.ref?.setValue(foodIntake)?.await()
            if (updateResult != null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Food intake not found in Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class DailyCalories(
    val date: String,
    val totalCalories: Int
)