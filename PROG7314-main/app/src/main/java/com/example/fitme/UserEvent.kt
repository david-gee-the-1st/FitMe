package com.example.fitme

sealed interface UserEvent {
    object createUser : UserEvent
    data class setUsername(val username: String) : UserEvent
    data class setEmail(val email: String) : UserEvent
    data class setHeight(val height: Double) : UserEvent
    data class setWeight(val weight: Double) : UserEvent
    data class setDob(val dob: String) : UserEvent
    data class setPhone(val phone: String) : UserEvent
    data class setPassword(val password: String) : UserEvent
    data class deleteUser(val user: User) : UserEvent
}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */