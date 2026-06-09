package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM wallets WHERE user_id = :userId")
    fun getWallet(userId: String = "user_1"): Flow<WalletEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWallet(wallet: WalletEntity)
    
    @Query("SELECT * FROM transactions WHERE wallet_id = :userId ORDER BY timestamp DESC LIMIT 5")
    fun getRecentTransactions(userId: String = "user_1"): Flow<List<TransactionEntity>>
    
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM user_profile WHERE id = :userId")
    fun getUserProfile(userId: String = "user_1"): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(profile: UserProfileEntity)
}
