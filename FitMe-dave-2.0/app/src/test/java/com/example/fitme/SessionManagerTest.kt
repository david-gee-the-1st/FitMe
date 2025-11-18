package com.example.fitme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private lateinit var session: SessionManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        session = SessionManager(context)
        session.clearSession()
    }

    @Test
    fun `session saves and retrieves user correctly`() {
        session.saveUserSession(1, "email@test.com", "TestUser")
        assertTrue(session.isLoggedIn())
        assertEquals(1, session.getUserId())
    }

    @Test
    fun `session clears successfully`() {
        session.saveUserSession(1, "email@test.com", "TestUser")
        session.clearSession()
        assertFalse(session.isLoggedIn())
    }
}
