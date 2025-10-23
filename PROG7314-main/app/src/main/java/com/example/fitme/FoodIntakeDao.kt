package com.example.fitme

import androidx.room.*

@Dao
interface FoodIntakeDao {
    @Insert
    suspend fun addIntake(food: FoodIntake)

    @Query("SELECT * FROM food_intake WHERE date = :date")
    suspend fun getTodayIntake(date: String): List<FoodIntake>

    @Query("SELECT SUM(calories) FROM food_intake WHERE date = :date")
    suspend fun getTotalCalories(date: String): Int?

    @Query("SELECT * FROM food_intake WHERE foodName LIKE :query")
    suspend fun searchFood(query: String): List<FoodIntake>

    @Query("""
        SELECT date, SUM(calories) as totalCalories
        FROM food_intake
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    suspend fun getCaloriesForLast7Days(startDate: String, endDate: String): List<CaloriesByDate>
}

/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */