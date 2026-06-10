package com.example.data.repository

import com.example.data.local.ConversationDao
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageDao
import com.example.data.local.MessageEntity
import com.example.data.local.PendingSyncDao
import com.example.data.local.PendingSyncEntity
import com.example.data.local.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineChatRepository(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val pendingSyncDao: PendingSyncDao,
) {
    fun getConversations(): Flow<List<ConversationEntity>> = conversationDao.getAll()

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> = messageDao.getMessagesByConversationId(conversationId)

    suspend fun sendMessage(message: MessageEntity) {
        // Single source of truth: Write directly to Room for instant UI update
        messageDao.insert(message.copy(isSynced = false))
        
        // Queue operation
        val syncOp = PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            operationType = "INSERT",
            tableName = "messages",
            recordId = message.id,
            payload = "" // Could serialize to JSON here based on user needs
        )
        pendingSyncDao.insert(syncOp)
    }

    suspend fun createConversation(conversation: ConversationEntity) {
        // Single source of truth: Write directly to Room
        conversationDao.insert(conversation.copy(isSynced = false))
        
        // Queue operation
        val syncOp = PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            operationType = "INSERT",
            tableName = "conversations",
            recordId = conversation.id,
            payload = "" // Could serialize to JSON here
        )
        pendingSyncDao.insert(syncOp)
    }

    suspend fun sync() {
        val pendingOps = pendingSyncDao.getPendingOperations()
        for (op in pendingOps) {
            try {
                // Execute network operation
                // For now, assume success
                
                // On success, clean up operation
                pendingSyncDao.deleteOperation(op.id)
                
                // Next: Mark local record as synced via its DAO
            } catch (e: Exception) {
                // Keep pending, error state will be picked up on next retry
            }
        }
    }
}
