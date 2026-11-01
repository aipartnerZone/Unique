package com.example.data.local

import kotlinx.coroutines.flow.Flow

class MunnaRepository(private val dao: MunnaDao) {
    val commandHistory: Flow<List<CommandHistoryEntity>> = dao.getAllCommandHistory()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()
    val userMemories: Flow<List<UserMemoryEntity>> = dao.getAllUserMemories()

    suspend fun logCommand(
        recognizedText: String,
        normalizedCommand: String,
        intentCategory: String,
        actionStatus: String,
        spokenResponse: String,
        isSuccess: Boolean
    ) {
        dao.insertCommand(
            CommandHistoryEntity(
                recognizedText = recognizedText,
                normalizedCommand = normalizedCommand,
                intentCategory = intentCategory,
                actionStatus = actionStatus,
                spokenResponse = spokenResponse,
                isSuccess = isSuccess
            )
        )
    }

    suspend fun deleteCommand(id: Long) {
        dao.deleteCommandById(id)
    }

    suspend fun clearCommandHistory() {
        dao.clearAllCommands()
    }

    suspend fun addChatMessage(sender: String, messageText: String, isError: Boolean = false) {
        dao.insertChatMessage(
            ChatMessageEntity(
                sender = sender,
                messageText = messageText,
                isError = isError
            )
        )
    }

    suspend fun clearChatHistory() {
        dao.clearAllChatMessages()
    }

    suspend fun saveLearnedMemory(
        category: String,
        memoryKey: String,
        memoryValue: String,
        contactPhone: String = ""
    ) {
        dao.deleteMemoryByKey(memoryKey)
        dao.insertUserMemory(
            UserMemoryEntity(
                category = category,
                memoryKey = memoryKey,
                memoryValue = memoryValue,
                contactPhone = contactPhone
            )
        )
    }

    suspend fun getMemoriesSnapshot(): List<UserMemoryEntity> {
        return dao.getUserMemoriesSnapshot()
    }

    suspend fun deleteMemory(id: Long) {
        dao.deleteUserMemoryById(id)
    }

    suspend fun clearAllMemories() {
        dao.clearAllUserMemories()
    }
}
