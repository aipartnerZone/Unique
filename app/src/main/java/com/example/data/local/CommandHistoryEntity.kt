package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val recognizedText: String,
    val normalizedCommand: String,
    val intentCategory: String,
    val actionStatus: String,
    val spokenResponse: String,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
