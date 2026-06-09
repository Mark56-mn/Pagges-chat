package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class AppRepository(private val dao: AppDao) {
    val wallet: Flow<WalletEntity?> = dao.getWallet()
    val recentTransactions: Flow<List<TransactionEntity>> = dao.getRecentTransactions()
    val conversations: Flow<List<ConversationEntity>> = dao.getConversations()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    fun getMessages(conversationId: String) = dao.getMessages(conversationId)

    suspend fun initializeDataIfEmpty() {
        val currentWallet = dao.getWallet().firstOrNull()
        if (currentWallet == null) {
            dao.upsertWallet(WalletEntity(user_id = "user_1", balance = 5000.0))
            dao.insertTransaction(
                TransactionEntity(
                    amount = 5000.0,
                    party_name = "Initial Deposit",
                    type = "RECEIVED",
                    timestamp = System.currentTimeMillis() - 100000 
                )
            )
        }

        val profile = dao.getUserProfile().firstOrNull()
        if (profile == null) {
            dao.upsertUserProfile(UserProfileEntity(display_name = "Jane Doe", bio = "I love using Pagges Chat!"))
        }

        val convos = dao.getConversations().firstOrNull()
        if (convos.isNullOrEmpty()) {
            val conv1 = ConversationEntity(id = "c1", other_party_name = "Marcus Wright", other_party_initials = "M", last_message = "Did you check the new wallet integration?")
            val conv2 = ConversationEntity(id = "c2", other_party_name = "Team Standup", other_party_initials = "T", last_message = "Jason: The profile screen is finished...")
            
            dao.upsertConversation(conv1)
            dao.upsertConversation(conv2)

            dao.insertMessage(MessageEntity(conversation_id = "c1", text = "Hey Marcus, how is it going?", is_from_me = true, timestamp = System.currentTimeMillis() - 3600000))
            dao.insertMessage(MessageEntity(conversation_id = "c1", text = "Did you check the new wallet integration?", is_from_me = false, timestamp = System.currentTimeMillis() - 3500000))

            dao.insertMessage(MessageEntity(conversation_id = "c2", text = "Jason: The profile screen is finished...", is_from_me = false, timestamp = System.currentTimeMillis() - 7200000))
        }
    }

    suspend fun updateUserProfile(name: String, bio: String) {
        val current = dao.getUserProfile().firstOrNull() ?: UserProfileEntity()
        dao.upsertUserProfile(current.copy(display_name = name, bio = bio))
    }

    suspend fun sendMessage(conversationId: String, text: String, isAi: Boolean = false) {
        val message = MessageEntity(conversation_id = conversationId, text = text, is_from_me = true, is_ai = isAi)
        dao.insertMessage(message)
        
        // Update conversation last message
        val convs = dao.getConversations().firstOrNull()
        val conv = convs?.find { it.id == conversationId }
        if (conv != null) {
            dao.upsertConversation(conv.copy(last_message = text, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun receiveMessage(conversationId: String, text: String, isAi: Boolean = false) {
        val message = MessageEntity(conversation_id = conversationId, text = text, is_from_me = false, is_ai = isAi)
        dao.insertMessage(message)
        
        // Update conversation last message
        val convs = dao.getConversations().firstOrNull()
        val conv = convs?.find { it.id == conversationId }
        if (conv != null) {
            dao.upsertConversation(conv.copy(last_message = text, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun addFunds(amount: Double) {
        val currentWallet = dao.getWallet().firstOrNull()
        if (currentWallet != null) {
            val updatedBalance = currentWallet.balance + amount
            dao.upsertWallet(currentWallet.copy(balance = updatedBalance))
            dao.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    party_name = "Deposit",
                    type = "RECEIVED",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sendMoney(amount: Double, recipient: String) {
        val currentWallet = dao.getWallet().firstOrNull()
        if (currentWallet != null && currentWallet.balance >= amount) {
            val updatedBalance = currentWallet.balance - amount
            dao.upsertWallet(currentWallet.copy(balance = updatedBalance))
            dao.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    party_name = recipient,
                    type = "SENT",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
