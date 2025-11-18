package com.example.fitme

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitme.databinding.ActivityProgressBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Progress : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityProgressBinding
    private lateinit var foodIntakeDao: FoodIntakeDao
    private lateinit var foodViewModel: FoodIntakeViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: FoodAdapter
    private val foodList = mutableListOf<AuthResponse>()

    private val viewModel: ProgressViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProgressViewModel(foodIntakeDao) as T
            }
        }
        ViewModelProvider(this, factory)[ProgressViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system bar insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        foodViewModel = ViewModelProvider(this)[FoodIntakeViewModel::class.java]

        // Initialize DB
        foodIntakeDao = FitMeDatabase.getDatabase(this).foodIntakeDao()

        // Observe and load data
        observeViewModel()
        viewModel.onEvent(ProgressEvent.LoadProgressData)

        // Handle add intake button
        binding.btnAddIntake.setOnClickListener {
            val intent = Intent(this, AddIntake::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.foodListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FoodAdapter(foodList)
        recyclerView.adapter = adapter

        setupBottomNavigation()
        fetchFoodsFromApi()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.tvCaloriesValue.text = "${state.totalCalories} Cals"
                binding.progressCalories.progress = state.totalCalories
                binding.tvTotalCalories.text = "Total: ${state.totalCalories}"

                // Display list
                displayFoodList(state.todayIntake)

                // Draw line chart
                drawLineChart(state.days, state.weeklyCalories)

                // Show error (if any)
                state.errorMessage?.let {
                    Toast.makeText(this@Progress, it, Toast.LENGTH_SHORT).show()
                }
            }
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
                    startActivity(Intent(this, AddIntake::class.java))
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

    private fun updateLineChartWithToday(todayCalories: Double) {
        val lineChart: LineChart = binding.lineChart

        val entries = mutableListOf<Entry>()

        // Add previous data (fake or existing from ViewModel)
        // Example: Replace with actual weekly data if available
        entries.add(Entry(0f, 1200f)) // Monday
        entries.add(Entry(1f, 1500f)) // Tuesday
        entries.add(Entry(2f, 1300f)) // Wednesday
        entries.add(Entry(3f, 1600f)) // Thursday
        entries.add(Entry(4f, 1400f)) // Friday

        //Add today (new entry)
        val todayIndex = entries.size
        entries.add(Entry(todayIndex.toFloat(), todayCalories.toFloat()))

        val dataSet = LineDataSet(entries, "Calorie Intake").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.RED)
            setDrawValues(true)
        }

        lineChart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Today")
        )

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }



    private fun calculateTodayCalories(foodList: List<AuthResponse>): Double {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        return foodList
            .filter { it.createdAt?.startsWith(today) == true }
            .sumOf { it.calories ?: 0.0 }
    }

    private fun fetchFoodsFromApi(): Double  {
        Toast.makeText(this, "Fetching foods...", Toast.LENGTH_SHORT).show()

        var todayCalories: Double = 0.0

        val call = ApiClient.authApi.getAllFoods()
        call.enqueue(object : Callback<FoodsResponse> {
            override fun onResponse(call: Call<FoodsResponse>, response: Response<FoodsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    val currentUserId = sessionManager.getUserId()

                    // Filter for current user
                    val filteredFoods = data.filter { it.userID == currentUserId }

                    foodList.clear()
                    foodList.addAll(filteredFoods)
                    adapter.updateData(foodList)

                    // Calculate today's calories
                    val todayCalories = calculateTodayCalories(filteredFoods)

                    // Update UI elements
                    binding.tvCaloriesValue.text = "${todayCalories} Cals"
                    binding.tvTotalCalories.text = "Total: ${todayCalories}"
                    binding.progressCalories.progress = todayCalories.toInt()

                    // Send today's calories to chart
                    updateLineChartWithToday(todayCalories)

                    Toast.makeText(this@Progress, "Fetched ${filteredFoods.size} items!", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Fetched ${filteredFoods.size} items for user $currentUserId")
                }
            }

            override fun onFailure(call: Call<FoodsResponse>, t: Throwable) {
                Toast.makeText(this@Progress, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error: ${t.message}")
            }
        })

        return todayCalories
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