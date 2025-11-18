package com.example.fitme

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_intake")
data class FoodIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val calories: Int,
    val date: String
)

/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
Retrofit Android Tutorial - Make API Calls. 2023. YouTube video, added by Ahmed Guedmioui. [Online]. Available at: https://www.youtube.com/watch?v=8IhNq0ng-wk [Accessed 29 September 2025].
 */