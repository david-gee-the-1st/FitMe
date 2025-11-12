package com.example.fitme

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserViewModelTest {

    private lateinit var viewModel: UserViewModel

    @Before
    fun setup() {
        viewModel = UserViewModel(null)
    }

    @Test
    fun `hashPass should produce non-empty hashed string`() {
        val hash = viewModel.hashPass("mypassword")
        assertTrue(hash.isNotEmpty())
    }

    @Test
    fun `verifyPassword returns true for correct password`() {
        val original = "secret123"
        val hashed = viewModel.hashPass(original)
        assertTrue(viewModel.verifyPassword(original, hashed))
    }

    @Test
    fun `verifyPassword returns false for wrong password`() {
        val hashed = viewModel.hashPass("secret123")
        assertFalse(viewModel.verifyPassword("wrongpass", hashed))
    }
}
