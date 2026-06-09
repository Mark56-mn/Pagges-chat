package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val user_id: String = "user_1",
    val balance: Double = 0.0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val wallet_id: String = "user_1",
    val amount: Double,
    val party_name: String,
    val type: String, // "SENT" or "RECEIVED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val other_party_name: String,
    val other_party_initials: String,
    val last_message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversation_id: String,
    val text: String,
    val is_from_me: Boolean,
    val is_ai: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "user_1",
    val display_name: String = "User",
    val bio: String = ""
)
