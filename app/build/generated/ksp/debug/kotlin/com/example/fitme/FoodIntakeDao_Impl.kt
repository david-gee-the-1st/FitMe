package com.example.fitme

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FoodIntakeDao_Impl(
  __db: RoomDatabase,
) : FoodIntakeDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFoodIntake: EntityInsertAdapter<FoodIntake>
  init {
    this.__db = __db
    this.__insertAdapterOfFoodIntake = object : EntityInsertAdapter<FoodIntake>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `food_intake` (`id`,`foodName`,`calories`,`date`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FoodIntake) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.foodName)
        statement.bindLong(3, entity.calories.toLong())
        statement.bindText(4, entity.date)
      }
    }
  }

  public override suspend fun addIntake(food: FoodIntake): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFoodIntake.insert(_connection, food)
  }

  public override suspend fun getTodayIntake(date: String): List<FoodIntake> {
    val _sql: String = "SELECT * FROM food_intake WHERE date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFoodName: Int = getColumnIndexOrThrow(_stmt, "foodName")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: MutableList<FoodIntake> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodIntake
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpFoodName: String
          _tmpFoodName = _stmt.getText(_columnIndexOfFoodName)
          val _tmpCalories: Int
          _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          _item = FoodIntake(_tmpId,_tmpFoodName,_tmpCalories,_tmpDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalCalories(date: String): Int? {
    val _sql: String = "SELECT SUM(calories) FROM food_intake WHERE date = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun searchFood(query: String): List<FoodIntake> {
    val _sql: String = "SELECT * FROM food_intake WHERE foodName LIKE ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFoodName: Int = getColumnIndexOrThrow(_stmt, "foodName")
        val _columnIndexOfCalories: Int = getColumnIndexOrThrow(_stmt, "calories")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _result: MutableList<FoodIntake> = mutableListOf()
        while (_stmt.step()) {
          val _item: FoodIntake
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpFoodName: String
          _tmpFoodName = _stmt.getText(_columnIndexOfFoodName)
          val _tmpCalories: Int
          _tmpCalories = _stmt.getLong(_columnIndexOfCalories).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          _item = FoodIntake(_tmpId,_tmpFoodName,_tmpCalories,_tmpDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCaloriesForLast7Days(startDate: String, endDate: String): List<CaloriesByDate> {
    val _sql: String = """
        |
        |        SELECT date, SUM(calories) as totalCalories
        |        FROM food_intake
        |        WHERE date BETWEEN ? AND ?
        |        GROUP BY date
        |        ORDER BY date ASC
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, startDate)
        _argIndex = 2
        _stmt.bindText(_argIndex, endDate)
        val _columnIndexOfDate: Int = 0
        val _columnIndexOfTotalCalories: Int = 1
        val _result: MutableList<CaloriesByDate> = mutableListOf()
        while (_stmt.step()) {
          val _item: CaloriesByDate
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTotalCalories: Int
          _tmpTotalCalories = _stmt.getLong(_columnIndexOfTotalCalories).toInt()
          _item = CaloriesByDate(_tmpDate,_tmpTotalCalories)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
