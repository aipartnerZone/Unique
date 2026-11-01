package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores personal facts, study preferences, and quick-call contacts that Munna AI
 * learns from the user's voice commands and conversations ("ye hamre data se sikhega").
 */
@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,          // e.g., "प्रोफ़ाइल (Profile)", "पढ़ाई (Study)", "कॉल संपर्क (Contact)", "आदत (Habit)"
    val memoryKey: String,         // e.g., "user_name", "student_class", "contact:papa", "fact_..."
    val memoryValue: String,       // Human-readable learned fact or phone number
    val contactPhone: String = "", // Optional phone number if this memory is a saved voice-call contact
    val timestamp: Long = System.currentTimeMillis()
)
