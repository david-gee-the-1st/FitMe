package com.example.fitme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fitme.databinding.ActivityProgressBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class Progress : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var foodIntakeDao: FoodIntakeDao
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    // ViewModel
    private val viewModel: ProgressViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val foodIntakeFirebaseRepo = FoodIntakeFirebaseRepository()
                val userFirebaseRepo = UserFirebaseRepository()
                return ProgressViewModel(foodIntakeDao, foodIntakeFirebaseRepo, userFirebaseRepo) as T
            }
        }
        ViewModelProvider(this, factory)[ProgressViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize DB + DAOs
        val database = FitMeDatabase.getDatabase(this)
        foodIntakeDao = database.foodIntakeDao()
        userDao = database.userDao()
        sessionManager = SessionManager(this)

        // Observe view model state
        observeViewModel()

        // Load progress data
        viewModel.onEvent(ProgressEvent.LoadProgressData)

        // Add intake button → AddIntake screen
        binding.btnAddIntake.setOnClickListener {
            startActivity(Intent(this, AddIntake::class.java))
        }

        // Bottom Navigation
        setupBottomNavigation()
    }

    private fun observeViewModel() {

        lifecycleScope.launch {
            viewModel.state.collect { state ->

                // Daily calories
                val total = state.totalCalories
                binding.tvCaloriesValue.text = "$total Cals"

                // Set progress bar dynamically
                loadUserCalorieLimit { limit ->

                    binding.progressCalories.max = limit
                    binding.progressCalories.progress = total
                }

                // List of today's foods
                displayFoodList(state.todayIntake)

                // Weekly chart
                drawLineChart(state.days, state.weeklyCalories)

                // Error message
                state.errorMessage?.let {
                    Toast.makeText(this@Progress, it, Toast.LENGTH_SHORT).show()
                }

                // Sync to User DB for Profile page summary
                syncCaloriesToUser(total)
            }
        }
    }

    // Sync daily total calories to "caloriesToday" in User table
    private fun syncCaloriesToUser(total: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = sessionManager.getUserId()
            if (!userId.isNullOrBlank()) {
                val user = userDao.getUserById(userId)
                if (user != null) {
                    val updated = user.copy(caloriesToday = total)
                    userDao.upsertUser(updated)
                    // Also sync to Firebase
                    viewModel.updateUserCalories(userId, total)
                }
            }
        }
    }

    // Load user calorie limit to set progress bar max value
    private fun loadUserCalorieLimit(callback: (limit: Int) -> Unit) {

        lifecycleScope.launch(Dispatchers.IO) {
            val userId = sessionManager.getUserId()
            val user = if (!userId.isNullOrBlank()) userDao.getUserById(userId) else null

            val limit = user?.calorieLimit ?: 2000   // fallback to 2000

            withContext(Dispatchers.Main) { callback(limit) }
        }
    }

    private fun displayFoodList(foodList: List<FoodIntake>) {
        binding.foodListRecyclerView.adapter = FoodIntakeAdapter(foodList)
    }

    private fun drawLineChart(days: List<String>, calories: List<Int>) {

        val lineChart: LineChart = binding.lineChart

        val entries = calories.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val dataSet = LineDataSet(entries, "Calorie Intake").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.RED)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(days)
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -45f
        xAxis.textColor = Color.BLACK

        lineChart.axisLeft.textColor = Color.BLACK
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.invalidate()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = binding.bottomNavigationView
        bottomNavigation.selectedItemId = R.id.nav_progress

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }

                R.id.nav_progress -> true

                R.id.nav_camera -> {
                    startActivity(Intent(this, Camera::class.java))
                    true
                }

                R.id.nav_search -> {
                    startActivity(Intent(this, AddIntake::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }

                else -> false
            }
        }
    }
}

/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
Retrofit Android Tutorial - Make API Calls. 2023. YouTube video, added by Ahmed Guedmioui. [Online]. Available at: https://www.youtube.com/watch?v=8IhNq0ng-wk [Accessed 29 September 2025].
To Do List App using Recycler View Android Studio Kotlin Example Tutorial. 2022. YouTube video, added by Code With Cal. [Online]. Available at: https://www.youtube.com/watch?v=RfIR4oaSVfQ [Accessed 20 September 2025].
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
how to create line chart | MP Android Chart | Android Studio 2024. 2023. YouTube video, added by Easy One Coder. [Online]. Available at: https://www.youtube.com/watch?v=KIW4Vp8mjLo [Accessed 20 September 2025].
 */