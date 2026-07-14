package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface PinDao {
    @Query("SELECT * FROM pins ORDER BY createdAt DESC")
    fun getAllPins(): Flow<List<Pin>>

    @Query("SELECT * FROM pins WHERE category = :category ORDER BY createdAt DESC")
    fun getPinsByCategory(category: String): Flow<List<Pin>>

    @Query("SELECT * FROM pins WHERE boardId = :boardId ORDER BY createdAt DESC")
    fun getPinsByBoard(boardId: Int): Flow<List<Pin>>

    @Query("SELECT * FROM pins WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedPins(): Flow<List<Pin>>

    @Query("SELECT * FROM pins WHERE id = :id")
    suspend fun getPinById(id: Int): Pin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: Pin): Long

    @Update
    suspend fun updatePin(pin: Pin)

    @Delete
    suspend fun deletePin(pin: Pin)
}

@Dao
interface BoardDao {
    @Query("SELECT * FROM boards ORDER BY createdAt DESC")
    fun getAllBoards(): Flow<List<Board>>

    @Query("SELECT * FROM boards WHERE id = :id")
    suspend fun getBoardById(id: Int): Board?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: Board): Long

    @Delete
    suspend fun deleteBoard(board: Board)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE pinId = :pinId ORDER BY createdAt ASC")
    fun getCommentsForPin(pinId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long
}

@Database(entities = [Pin::class, Board::class, Comment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinDao(): PinDao
    abstract fun boardDao(): BoardDao
    abstract fun commentDao(): CommentDao
}
