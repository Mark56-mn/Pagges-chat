package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val participantName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val isSynced: Boolean = false
)
