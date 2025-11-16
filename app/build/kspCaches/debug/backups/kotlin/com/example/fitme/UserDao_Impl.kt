package com.example.fitme

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserDao_Impl(
  __db: RoomDatabase,
) : UserDao {
  private val __db: RoomDatabase

  private val __deleteAdapterOfUser: EntityDeleteOrUpdateAdapter<User>

  private val __upsertAdapterOfUser: EntityUpsertAdapter<User>
  init {
    this.__db = __db
    this.__deleteAdapterOfUser = object : EntityDeleteOrUpdateAdapter<User>() {
      protected override fun createQuery(): String = "DELETE FROM `users` WHERE `userId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: User) {
        statement.bindText(1, entity.userId)
      }
    }
    this.__upsertAdapterOfUser = EntityUpsertAdapter<User>(object : EntityInsertAdapter<User>() {
      protected override fun createQuery(): String = "INSERT INTO `users` (`userId`,`username`,`email`,`height`,`weight`,`dob`,`phone`,`password`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: User) {
        statement.bindText(1, entity.userId)
        statement.bindText(2, entity.username)
        statement.bindText(3, entity.email)
        statement.bindDouble(4, entity.height)
        statement.bindDouble(5, entity.weight)
        statement.bindText(6, entity.dob)
        statement.bindText(7, entity.phone)
        statement.bindText(8, entity.password)
      }
    }, object : EntityDeleteOrUpdateAdapter<User>() {
      protected override fun createQuery(): String = "UPDATE `users` SET `userId` = ?,`username` = ?,`email` = ?,`height` = ?,`weight` = ?,`dob` = ?,`phone` = ?,`password` = ? WHERE `userId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: User) {
        statement.bindText(1, entity.userId)
        statement.bindText(2, entity.username)
        statement.bindText(3, entity.email)
        statement.bindDouble(4, entity.height)
        statement.bindDouble(5, entity.weight)
        statement.bindText(6, entity.dob)
        statement.bindText(7, entity.phone)
        statement.bindText(8, entity.password)
        statement.bindText(9, entity.userId)
      }
    })
  }

  public override suspend fun deleteUser(user: User): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfUser.handle(_connection, user)
  }

  public override suspend fun upsertUser(user: User): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfUser.upsert(_connection, user)
  }

  public override suspend fun getUserById(userId: String): User? {
    val _sql: String = "SELECT * FROM users WHERE userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfDob: Int = getColumnIndexOrThrow(_stmt, "dob")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _result: User?
        if (_stmt.step()) {
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpHeight: Double
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpDob: String
          _tmpDob = _stmt.getText(_columnIndexOfDob)
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          _result = User(_tmpUserId,_tmpUsername,_tmpEmail,_tmpHeight,_tmpWeight,_tmpDob,_tmpPhone,_tmpPassword)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUserByUsername(username: String): User? {
    val _sql: String = "SELECT * FROM users WHERE username = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfDob: Int = getColumnIndexOrThrow(_stmt, "dob")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _result: User?
        if (_stmt.step()) {
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpHeight: Double
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpDob: String
          _tmpDob = _stmt.getText(_columnIndexOfDob)
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          _result = User(_tmpUserId,_tmpUsername,_tmpEmail,_tmpHeight,_tmpWeight,_tmpDob,_tmpPhone,_tmpPassword)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUserByEmail(email: String): User? {
    val _sql: String = "SELECT * FROM users WHERE email = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, email)
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfHeight: Int = getColumnIndexOrThrow(_stmt, "height")
        val _columnIndexOfWeight: Int = getColumnIndexOrThrow(_stmt, "weight")
        val _columnIndexOfDob: Int = getColumnIndexOrThrow(_stmt, "dob")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _result: User?
        if (_stmt.step()) {
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpHeight: Double
          _tmpHeight = _stmt.getDouble(_columnIndexOfHeight)
          val _tmpWeight: Double
          _tmpWeight = _stmt.getDouble(_columnIndexOfWeight)
          val _tmpDob: String
          _tmpDob = _stmt.getText(_columnIndexOfDob)
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          _result = User(_tmpUserId,_tmpUsername,_tmpEmail,_tmpHeight,_tmpWeight,_tmpDob,_tmpPhone,_tmpPassword)
        } else {
          _result = null
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
