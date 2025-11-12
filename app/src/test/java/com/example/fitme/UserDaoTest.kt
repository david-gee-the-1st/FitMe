package com.example.fitme


import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserDaoTest {
    private lateinit var db: FitMeDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, FitMeDatabase::class.java).build()
        dao = db.userDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `insert and retrieve user`() = runBlocking {
        val user = User(username = "John", email = "john@mail.com", password = "hashed", height = 1.8, weight = 80.0, dob = "2000-01-01", phone = "000")
        dao.upsertUser(user)
        val result = dao.getUserByUsername("John")
        assertNotNull(result)
        assertEquals("john@mail.com", result?.email)
    }
}
