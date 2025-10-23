package com.example.fitme

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FoodIntakeDaoTest {
    private lateinit var db: FitMeDatabase
    private lateinit var dao: FoodIntakeDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, FitMeDatabase::class.java).build()
        dao = db.foodIntakeDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `add and fetch food intake`() = runBlocking {
        val intake = FoodIntake(foodName = "Apple", calories = 95, date = "2025-10-08")
        dao.addIntake(intake)
        val result = dao.getAllIntakes()
        assertTrue(result.isNotEmpty())
        assertEquals("Apple", result.first().foodName)
    }
}
