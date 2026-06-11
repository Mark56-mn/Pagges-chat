package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val display_name: String,
    val bio: String,
    val avatar_url: String? = null
)

@Serializable
data class Wallet(
    val user_id: String,
    val balance: Double
)

@Serializable
data class Transaction(
    val id: String = "",
    val user_id: String,
    val amount: Double,
    val party_name: String,
    val type: String,
    val timestamp: Long
)

@Serializable
data class Conversation(
    val id: String,
    val other_party_name: String,
    val last_message: String,
    val timestamp: Long,
    val avatar_url: String? = null
)

@Serializable
data class Message(
    val id: String = "",
    val conversation_id: String,
    val text: String,
    val is_from_me: Boolean,
    val is_ai: Boolean,
    val timestamp: Long,
    val image_url: String? = null,
    val voice_note_url: String? = null,
    val reactions: String? = null
)

@Serializable
data class TransferFundsRequest(
    val sender_id: String,
    val receiver_id: String,
    val amount: Double
)

@Serializable
data class TransferFundsResponse(
    val success: Boolean,
    val error: String? = null,
    val message: String? = null,
    val new_sender_balance: Double? = null,
    val new_receiver_balance: Double? = null
)
