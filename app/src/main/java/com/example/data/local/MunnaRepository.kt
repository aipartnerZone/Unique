package com.example.data.local

import kotlinx.coroutines.flow.Flow

class MunnaRepository(private val dao: MunnaDao) {
    val commandHistory: Flow<List<CommandHistoryEntity>> = dao.getAllCommandHistory()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

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
}
