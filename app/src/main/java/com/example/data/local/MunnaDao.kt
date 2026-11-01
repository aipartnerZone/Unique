package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MunnaDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC")
    fun getAllCommandHistory(): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CommandHistoryEntity)

    @Query("DELETE FROM command_history WHERE id = :id")
    suspend fun deleteCommandById(id: Long)

    @Query("DELETE FROM command_history")
    suspend fun clearAllCommands()

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllChatMessages()

    // Personal Agent Learned Memories & Quick Call Contacts
    @Query("SELECT * FROM user_memories ORDER BY timestamp DESC")
    fun getAllUserMemories(): Flow<List<UserMemoryEntity>>

    @Query("SELECT * FROM user_memories ORDER BY timestamp DESC")
    suspend fun getUserMemoriesSnapshot(): List<UserMemoryEntity>

    @Query("DELETE FROM user_memories WHERE memoryKey = :memoryKey")
    suspend fun deleteMemoryByKey(memoryKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserMemory(memory: UserMemoryEntity)

    @Query("DELETE FROM user_memories WHERE id = :id")
    suspend fun deleteUserMemoryById(id: Long)

    @Query("DELETE FROM user_memories")
    suspend fun clearAllUserMemories()
}
