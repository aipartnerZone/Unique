package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sender: String, // "USER" or "MUNNA"
    val messageText: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
