package com.example.fitme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressViewModel(
    private val foodIntakeDao: FoodIntakeDao,
    private val foodIntakeFirebaseRepo: FoodIntakeFirebaseRepository,
    private val userFirebaseRepo: UserFirebaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state

    private var currentUserId: String? = null

    fun setCurrentUser(userId: String) {
        currentUserId = userId
    }

    fun onEvent(event: ProgressEvent) {
        when (event) {
            is ProgressEvent.AddFoodIntake -> addFood(event.foodName, event.calories)
            is ProgressEvent.LoadTodayProgress -> loadTodayData()
            is ProgressEvent.LoadProgressData -> loadProgress()
        }
    }

    private fun addFood(foodName: String, calories: Int) {
        if (foodName.isBlank() || calories <= 0) {
            _state.value = _state.value.copy(errorMessage = "Enter valid food and calories")
            return
        }

        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val food = FoodIntake(foodName = foodName, calories = calories, date = date)

            try {
                foodIntakeDao.addIntake(food)

                val result = foodIntakeFirebaseRepo.pushFoodIntakeToFirebase(food)
                if (result.isFailure) {
                    _state.value = _state.value.copy(
                        errorMessage = "Firebase sync failed: ${result.exceptionOrNull()?.message}"
                    )
                } else {
                    currentUserId?.let { userId ->
                        val currentCalories = _state.value.totalCalories
                        userFirebaseRepo.updateUserCalories(userId, currentCalories + calories)
                    }
                }

                loadTodayData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Failed to add food: ${e.message}")
            }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = java.util.Calendar.getInstance()

                val endDate = sdf.format(calendar.time)
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -6)
                val startDate = sdf.format(calendar.time)

                val weeklyDataResult = foodIntakeFirebaseRepo.getWeeklyCalories(startDate, endDate)
                val weeklyData = if (weeklyDataResult.isSuccess) {
                    weeklyDataResult.getOrThrow()
                } else {
                    val localData = foodIntakeDao.getCaloriesForLast7Days(startDate, endDate)
                    localData.map { dailyCalories ->
                        DailyCalories(
                            date = dailyCalories.date,
                            totalCalories = dailyCalories.totalCalories
                        )
                    }
                }

                val map: Map<String, Int> = weeklyData.associateBy(
                    { it.date },
                    { it.totalCalories }
                )

                val weeklyCalories = mutableListOf<Int>()
                val days = mutableListOf<String>()
                val current = java.util.Calendar.getInstance()

                repeat(7) {
                    val date = sdf.format(current.time)
                    weeklyCalories.add(0, map[date] ?: 0)
                    days.add(0, date.substring(5))
                    current.add(java.util.Calendar.DAY_OF_YEAR, -1)
                }

                val today = sdf.format(Date())
                val todayList = foodIntakeDao.getTodayIntake(today)
                val totalToday = foodIntakeDao.getTotalCalories(today) ?: 0

                _state.value = ProgressState(
                    todayIntake = todayList,
                    totalCalories = totalToday,
                    weeklyCalories = weeklyCalories,
                    days = days,
                    isLoading = false
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading progress: ${e.message}"
                )
            }
        }
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val foods = foodIntakeDao.getTodayIntake(date)
                val total = foodIntakeDao.getTotalCalories(date) ?: 0

                _state.value = ProgressState(todayIntake = foods, totalCalories = total)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Failed to load data")
            }
        }
    }
    fun updateUserCalories(userId: String, calories: Int) {
        viewModelScope.launch {
            userFirebaseRepo.updateUserCalories(userId, calories)
        }
    }
}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
To Do List App using Recycler View Android Studio Kotlin Example Tutorial. 2022. YouTube video, added by Code With Cal. [Online]. Available at: https://www.youtube.com/watch?v=RfIR4oaSVfQ [Accessed 20 September 2025].
how to create line chart | MP Android Chart | Android Studio 2024. 2023. YouTube video, added by Easy One Coder. [Online]. Available at: https://www.youtube.com/watch?v=KIW4Vp8mjLo [Accessed 20 September 2025].
 */