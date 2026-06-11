package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val isSynced: Boolean = false,
    val imageUrl: String? = null,
    val voiceNoteUrl: String? = null,
    val isPinned: Boolean = false,
    val reactions: String? = null
)
