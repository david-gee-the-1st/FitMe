package com.example.fitme

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.widget.EditText
import android.widget.TextView

class AddIntake : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodAdapter
    private val foodList = mutableListOf<AuthResponse>()
    private lateinit var foodIntakeDao: FoodIntakeDao
    private lateinit var edtType: EditText
    private lateinit var btnAddFood: Button
    private lateinit var tvTotalSearches: TextView
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentDateShort(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_intake)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize database
        val database = FitMeDatabase.getDatabase(this)
        foodIntakeDao = database.foodIntakeDao()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.foodListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FoodAdapter(foodList)
        recyclerView.adapter = adapter
        edtType = findViewById(R.id.edtType)
        btnAddFood = findViewById(R.id.btnAddFood)
        tvTotalSearches = findViewById(R.id.tvTotalSearches)
        searchFeature()
        fetchFoodsFromApi()

        //Bottom Navigation View:
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_progress -> {
                    val intent = Intent(this, Progress::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_camera -> {
                    val intent = Intent(this, Camera::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_search -> {
                    //No action needed already already at Search
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_search

        //getFoodsFromApi()
        fetchFoodsFromApi()

        val btnSave = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveIntake)
        val etFoodName = findViewById<EditText>(R.id.etFoodName)
        val etCalories = findViewById<EditText>(R.id.etCalories)

        btnSave.setOnClickListener {
            val name = etFoodName.text.toString().trim()
            val caloriesText = etCalories.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a food name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (caloriesText.isEmpty()) {
                Toast.makeText(this, "Please enter calories", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calories = caloriesText.toDoubleOrNull() ?: 0.0

            if (calories <= 0) {
                Toast.makeText(this, "Please enter valid calories", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newFood = AuthResponse(
                _id = null,
                name = name,
                calories = calories,
                userID = "placeholder-user-id",
                createdAt = getCurrentDate(),
                updatedAt = getCurrentDate()
            )

            // Save to ALL THREE systems: API, Room, and Firebase
            saveToAllSystems(name, calories.toInt(), newFood)
        }
    }
    private fun searchFeature() {
        btnAddFood.setOnClickListener {
            val searchQuery = edtType.text.toString().trim()

            if (searchQuery.isEmpty()) {
                Toast.makeText(this, "Please enter food to search", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Filter the existing food list by search query
            filterFoodList(searchQuery)
        }
    }

    private fun filterFoodList(query: String) {
        val filteredList = foodList.filter { food ->
            food.name?.contains(query, ignoreCase = true) == true
        }.toMutableList()

        adapter.updateData(filteredList)
        updateSearchCount(foodList.size)
        updateSearchCount(filteredList.size)
    }

    private fun updateSearchCount(count: Int) {
        tvTotalSearches.text = "Total: $count"
    }
    private fun saveToAllSystems(foodName: String, calories: Int, apiFood: AuthResponse) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Save to Room Database (Local storage)
                val roomFood = FoodIntake(
                    foodName = foodName,
                    calories = calories,
                    date = getCurrentDateShort()
                )
                foodIntakeDao.addIntake(roomFood)
                Log.i(TAG, "Saved to Room: $foodName")

                // 2. Save to Firebase (Cloud sync)
                val foodIntakeFirebaseRepo = FoodIntakeFirebaseRepository()
                val firebaseResult = foodIntakeFirebaseRepo.pushFoodIntakeToFirebase(roomFood)
                if (firebaseResult.isSuccess) {
                    Log.i(TAG, "Saved to Firebase: $foodName")
                } else {
                    Log.e(TAG, "Failed to save to Firebase: ${firebaseResult.exceptionOrNull()?.message}")
                }

                // 3. Save to External API (Primary source)
                addFoodToApi(apiFood)

                runOnUiThread {
                    Toast.makeText(this@AddIntake, "$foodName saved successfully!", Toast.LENGTH_SHORT).show()
                    // Clear input fields
                    findViewById<EditText>(R.id.etFoodName).text.clear()
                    findViewById<EditText>(R.id.etCalories).text.clear()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error saving to all systems: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@AddIntake, "Error saving food: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchFoodsFromApi() {
        Toast.makeText(this, "Fetching foods...", Toast.LENGTH_SHORT).show()

        val call = ApiClient.authApi.getAllFoods()
        call.enqueue(object : Callback<FoodsResponse> {
            override fun onResponse(call: Call<FoodsResponse>, response: Response<FoodsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    foodList.clear()
                    foodList.addAll(data)
                    adapter.updateData(foodList)
                    Toast.makeText(this@AddIntake, "Fetched ${data.size} items!", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Fetched ${data.size} items!")

                    // Sync API data to Room and Firebase in background
                    syncApiDataToLocalAndFirebase(data)
                } else {
                    Toast.makeText(this@AddIntake, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG,"Failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<FoodsResponse>, t: Throwable) {
                Toast.makeText(this@AddIntake, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG,"Error: ${t.message}")
            }
        })
    }

    private fun syncApiDataToLocalAndFirebase(apiFoods: List<AuthResponse>) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val foodIntakeFirebaseRepo = FoodIntakeFirebaseRepository()

                apiFoods.forEach { apiFood ->
                    // Convert API food to Room entity
                    val roomFood = FoodIntake(
                        foodName = apiFood.name ?: "",
                        calories = apiFood.calories?.toInt() ?: 0,
                        date = apiFood.createdAt?.substring(0, 10) ?: getCurrentDateShort()
                    )

                    // Save to Room
                    foodIntakeDao.addIntake(roomFood)

                    // Save to Firebase
                    foodIntakeFirebaseRepo.pushFoodIntakeToFirebase(roomFood)
                }
                Log.i(TAG, "Synced ${apiFoods.size} items to Room and Firebase")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing API data: ${e.message}")
            }
        }
    }

    private fun addFoodToApi(food: AuthResponse) {
        ApiClient.authApi.addFood(food).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val addedFood = response.body()!!
                    foodList.add(addedFood)
                    adapter.updateData(foodList)

                    Toast.makeText(this@AddIntake, "${addedFood.name} added to API!", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "${addedFood.name} added successfully to API!")

                } else {
                    Toast.makeText(this@AddIntake, "API save failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG,"Failed to add food to API: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@AddIntake, "API Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG,"API Error: ${t.message}")
            }
        })
    }
    /*
        private fun getFoodsFromApi() {
            ApiClient.authApi.getAllFoods().enqueue(object : Callback<FoodsResponse> {
                override fun onResponse(call: Call<FoodsResponse>, response: Response<FoodsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val foodsResponse = response.body()!!
                        foodList.clear()
                        foodList.addAll(foodsResponse.data)

                        Toast.makeText(
                            this@Intake,
                            "Fetched ${foodList.size} food items successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@Intake,
                            "Failed to fetch foods: ${response.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FoodsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@Intake,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
     */
}

/*
Reference list:
Retrofit Android Tutorial - Make API Calls. 2023. YouTube video, added by Ahmed Guedmioui. [Online]. Available at: https://www.youtube.com/watch?v=8IhNq0ng-wk [Accessed 29 September 2025].
To Do List App using Recycler View Android Studio Kotlin Example Tutorial. 2022. YouTube video, added by Code With Cal. [Online]. Available at: https://www.youtube.com/watch?v=RfIR4oaSVfQ [Accessed 20 September 2025].
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025]. */