package com.example.fitme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest

class UserViewModel(private val dao: UserDao) : ViewModel() {

    private val _userState = MutableStateFlow(UserState())
    val userState = _userState.asStateFlow()

    fun hashPass(hashedPassword: String) : String {
        val bytes = hashedPassword.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun onEvent(event: UserEvent) {
        when (event) {
            is UserEvent.deleteUser -> {
                viewModelScope.launch {
                    dao.deleteUser(event.user)
                }
            }

            UserEvent.createUser -> {
                val userName = userState.value.username
                val password = userState.value.password
                val email = userState.value.email
                val height = userState.value.height
                val weight = userState.value.weight
                val dob = userState.value.dob
                val phone = userState.value.phone
                val checkedEmail = _userState.equals(UserState::isValid)
                val hashedPassword = hashPass(password)
                if(userName.isBlank() || password.isBlank() || email.isBlank())
                {
                    _userState.update { it.copy(errorMessage = "All fields are required.") }
                    return
                }
                if (checkedEmail) {
                    _userState.update { it.copy(errorMessage = "Invalid email address.") }
                    return
                }

                val user = User(
                    username = userName,
                    email = email,
                    password = hashedPassword,
                    height = height,
                    weight = weight,
                    dob = dob,
                    phone = phone,
                )

                viewModelScope.launch {
                    try {
                        dao.upsertUser(user)
                        _userState.update { it.copy(
                            username = "",
                            password = "",
                            email = "",
                            isSuccess = true,
                            errorMessage = null
                        ) }
                    } catch (e: Exception) {
                        e.printStackTrace()

                        _userState.update { it.copy(
                            errorMessage = "Account creation failed: ${e.message}",
                            isSuccess = false
                        ) }
                    }
                }
            }
            is UserEvent.setDob ->  {

                _userState.update { it.copy(
                    dob = event.dob,
                    errorMessage = null
                ) }
            }
            is UserEvent.setEmail ->  {

                _userState.update { it.copy(
                    email = event.email,
                    errorMessage = null
                ) }
            }
            is UserEvent.setHeight ->  {

                _userState.update { it.copy(
                    height = event.height,
                    errorMessage = null
                ) }
            }
            is UserEvent.setPassword ->  {

                _userState.update { it.copy(
                    password = event.password,
                    errorMessage = null
                ) }
            }
            is UserEvent.setPhone ->  {

                _userState.update { it.copy(
                    phone = event.phone,
                    errorMessage = null
                ) }
            }
            is UserEvent.setUsername ->  {

                _userState.update { it.copy(
                    username = event.username,
                    errorMessage = null
                ) }
            }
            is UserEvent.setWeight ->  {

                _userState.update { it.copy(
                    weight = event.weight,
                    errorMessage = null
                ) }
            }
        }
    }
}

/*
Reference list:
The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android. 2023. YouTube video, added by Philipp Lackner. [Online]. Available at: https://www.youtube.com/watch?v=bOd3wO0uFr8 [Accessed 22 September 2025].
 */
