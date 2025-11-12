package com.example.fitme

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.widget.EditText

class AddIntake : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodAdapter
    private val foodList = mutableListOf<AuthResponse>()

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.foodListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FoodAdapter(foodList)
        recyclerView.adapter = adapter

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
            val calories = etCalories.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a food name", Toast.LENGTH_SHORT).show()
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

            addFoodToApi(newFood)
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

    private fun addFoodToApi(food: AuthResponse) {
        ApiClient.authApi.addFood(food).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val addedFood = response.body()!!
                    foodList.add(addedFood)
                    adapter.updateData(foodList)

                    Toast.makeText(this@AddIntake, "${addedFood.name} added successfully!", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "${addedFood.name} added successfully!")

                    // Clear input fields
                    findViewById<EditText>(R.id.etFoodName).text.clear()
                    findViewById<EditText>(R.id.etCalories).text.clear()
                } else {
                    Toast.makeText(this@AddIntake, "Failed to add food: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG,"Failed to add food: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@AddIntake, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG,"Error: ${t.message}")
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
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
 */