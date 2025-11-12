package com.example.fitme

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId : String = UUID.randomUUID().toString(),
    val username : String,
    val email : String,
    val height : Double,
    val weight : Double,
    val dob : String,
    val phone : String,
    val password : String
)

/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */
