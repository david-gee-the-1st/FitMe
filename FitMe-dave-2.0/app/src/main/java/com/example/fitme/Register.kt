package com.example.fitme

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fitme.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: FitMeDatabase
    private lateinit var userDao: UserDao

    private val viewModel: UserViewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val userFirebaseRepo = UserFirebaseRepository()
                return UserViewModel(userDao, userFirebaseRepo) as T
            }
        }
        ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private var dob: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FitMeDatabase.getDatabase(this)
        userDao = database.userDao()

        setupEventListeners()
        observeViewModel()
    }

    private fun setupEventListeners() {
        binding.etUsername.onTextChanged { viewModel.onEvent(UserEvent.setUsername(it)) }
        binding.etEmail.onTextChanged { viewModel.onEvent(UserEvent.setEmail(it)) }
        binding.etPassword.onTextChanged { viewModel.onEvent(UserEvent.setPassword(it)) }
        binding.etHeight.onTextChanged {
            viewModel.onEvent(UserEvent.setHeight(it.toDoubleOrNull() ?: 0.0))
        }
        binding.etWeight.onTextChanged {
            viewModel.onEvent(UserEvent.setWeight(it.toDoubleOrNull() ?: 0.0))
        }
        binding.etPhone.onTextChanged { viewModel.onEvent(UserEvent.setPhone(it)) }

        binding.etDob.setOnClickListener { showDatePicker() }

        binding.btnRegister.setOnClickListener {
            viewModel.onEvent(UserEvent.setDob(dob))
            viewModel.onEvent(UserEvent.createUser)
        }

        binding.btnLoginNav.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dob = dateFormat.format(selectedDate.time)

                binding.etDob.setText(dob)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userState.collect { state ->
                state.errorMessage?.let { error ->
                    Toast.makeText(this@Register, error, Toast.LENGTH_SHORT).show()
                }

                if (state.isSuccess) {
                    Toast.makeText(this@Register, "Account created!", Toast.LENGTH_LONG).show()

                    binding.etUsername.text.clear()
                    binding.etEmail.text.clear()
                    binding.etPassword.text.clear()
                    binding.etHeight.text.clear()
                    binding.etWeight.text.clear()
                    binding.etDob.text.clear()
                    binding.etPhone.text.clear()

                    startActivity(Intent(this@Register, Home::class.java))
                    finish()
                }
            }
        }
    }

    private fun android.widget.EditText.onTextChanged(listener: (String) -> Unit) {
        this.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                listener(s.toString())
            }
        })
    }
}
/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
Retrofit Android Tutorial - Make API Calls. 2023. YouTube video, added by Ahmed Guedmioui. [Online]. Available at: https://www.youtube.com/watch?v=8IhNq0ng-wk [Accessed 29 September 2025].
To Do List App using Recycler View Android Studio Kotlin Example Tutorial. 2022. YouTube video, added by Code With Cal. [Online]. Available at: https://www.youtube.com/watch?v=RfIR4oaSVfQ [Accessed 20 September 2025].
Bottom Navigation Bar - Android Studio | Fragments | Kotlin | 2023. 2023. YouTube video, added by Foxandroid. [Online]. Available at: https://www.youtube.com/watch?v=L_6poZGNXOo [Accessed 20 September 2025].
Date Picker Dialog on Android. 2019. YouTube video, added by CodingWithMitch. [Online]. Available at: https://www.youtube.com/watch?v=AdTzD96AhE0 [Accessed 29 September 2025].
 */