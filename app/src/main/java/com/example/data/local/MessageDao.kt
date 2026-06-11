package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversationId(conversationId: String): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<MessageEntity>

    @Query("SELECT * FROM messages JOIN messages_fts ON messages.id = messages_fts.rowid WHERE messages_fts MATCH :query")
    suspend fun searchMessages(query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isPinned = 1")
    fun getPinnedMessages(conversationId: String): kotlinx.coroutines.flow.Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isPinned = :isPinned WHERE id = :messageId")
    suspend fun updatePinnedStatus(messageId: String, isPinned: Boolean)

    @Query("UPDATE messages SET reactions = :reactions WHERE id = :messageId")
    suspend fun updateReactions(messageId: String, reactions: String)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(conversationId: String): MessageEntity?
}
