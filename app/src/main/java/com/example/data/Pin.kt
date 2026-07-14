package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pins")
data class Pin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String,
    val isLocalResource: Boolean = true,
    val category: String,
    val boardId: Int? = null,
    val isSaved: Boolean = false,
    val author: String = "Infinity Creator",
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "boards")
data class Board(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val coverImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pinId: Int,
    val author: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)
