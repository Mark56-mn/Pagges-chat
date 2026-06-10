package com.example.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SupabaseManager
import com.example.model.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet: StateFlow<Wallet?> = _wallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _userProfile = MutableStateFlow<Profile?>(null)
    val userProfile: StateFlow<Profile?> = _userProfile.asStateFlow()

    private val messageFlows = mutableMapOf<String, MutableStateFlow<List<Message>>>()

    init {
        fetchData()
    }

    private fun getUserId(): String {
        return SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
    }

    private fun fetchData() {
        val userId = getUserId()
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            try {
                // Fetch Profile
                val profileResult = SupabaseManager.client.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeList<Profile>()
                if (profileResult.isNotEmpty()) {
                    _userProfile.value = profileResult.first()
                } else {
                    val newProfile = Profile(id = userId, display_name = "New User", bio = "")
                    SupabaseManager.client.postgrest["profiles"].insert(newProfile)
                    _userProfile.value = newProfile
                }

                // Fetch Wallet
                val walletResult = SupabaseManager.client.postgrest["wallets"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Wallet>()
                if (walletResult.isNotEmpty()) {
                    _wallet.value = walletResult.first()
                } else {
                    val newWallet = Wallet(user_id = userId, balance = 0.0)
                    SupabaseManager.client.postgrest["wallets"].upsert(newWallet)
                    _wallet.value = newWallet
                }

                // Fetch Transactions
                _transactions.value = SupabaseManager.client.postgrest["transactions"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Transaction>()

                // Fetch Conversations
                _conversations.value = SupabaseManager.client.postgrest["conversations"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Conversation>()

            } catch (e: Exception) {
                Log.e("AppViewModel", "Error fetching data", e)
            }
        }
    }

    private val _isTypingMap = mutableMapOf<String, MutableStateFlow<Boolean>>()

    fun getTypingState(conversationId: String): StateFlow<Boolean> {
        return _isTypingMap.getOrPut(conversationId) { MutableStateFlow(false) }.asStateFlow()
    }

    fun setTyping(conversationId: String, isTyping: Boolean) {
        viewModelScope.launch {
            try {
                val channel = SupabaseManager.client.channel("typing_$conversationId")
                channel.broadcast(event = "typing", message = mapOf("user_id" to getUserId(), "status" to isTyping))
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error broadcasting typing", e)
            }
        }
    }

    fun getMessages(conversationId: String): StateFlow<List<Message>> {
        val isNew = !messageFlows.containsKey(conversationId)
        val flow = messageFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        val typingFlow = _isTypingMap.getOrPut(conversationId) { MutableStateFlow(false) }
        
        if (isNew) {
            viewModelScope.launch {
                try {
                    flow.value = SupabaseManager.client.postgrest["messages"]
                        .select { filter { eq("conversation_id", conversationId) } }
                        .decodeList<Message>()

                    val channel = SupabaseManager.client.channel("messages_$conversationId")
                    val messageChanges = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                        table = "messages"
                        filter = "conversation_id=eq.$conversationId"
                    }
                    
                    messageChanges.onEach { action ->
                        val newMessage = action.decodeRecord<Message>()
                        val currentList = flow.value
                        if (currentList.none { it.id == newMessage.id }) {
                            flow.value = currentList + newMessage
                        }
                    }.launchIn(viewModelScope)
                    
                    val typingChannel = SupabaseManager.client.channel("typing_$conversationId")
                    typingChannel.broadcastFlow<Map<String, Any>>(event = "typing")
                        .onEach { action -> 
                             // To fix Unresolved reference 'message' or 'payload', print properties or try using it as action itself 
                             val data = action
                             val senderId = data["user_id"] as? String
                             val status = data["status"] as? Boolean ?: false
                             if (senderId != getUserId()) {
                                 typingFlow.value = status
                             }
                        }.launchIn(viewModelScope)

                    io.github.jan.supabase.realtime.Realtime // Just to make sure it's loaded
                    SupabaseManager.client.realtime.connect()
                    channel.subscribe()
                    typingChannel.subscribe()
                } catch (e: Exception) {
                    Log.e("AppViewModel", "Error fetching messages", e)
                }
            }
        }
        return flow.asStateFlow()
    }

    fun addFunds(amount: Double) {
        val currentWallet = _wallet.value ?: return
        viewModelScope.launch {
            try {
                val newWallet = currentWallet.copy(balance = currentWallet.balance + amount)
                SupabaseManager.client.postgrest["wallets"].upsert(newWallet)
                _wallet.value = newWallet

                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    user_id = getUserId(),
                    amount = amount,
                    party_name = "Deposit",
                    type = "RECEIVED",
                    timestamp = System.currentTimeMillis()
                )
                SupabaseManager.client.postgrest["transactions"].insert(newTx)
                _transactions.value = listOf(newTx) + _transactions.value
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error adding funds", e)
            }
        }
    }

    fun sendMoney(amount: Double, recipient: String, onResult: (Boolean, String) -> Unit) {
        val currentWallet = _wallet.value ?: run {
            onResult(false, "Wallet not loaded")
            return
        }
        if (currentWallet.balance < amount) {
            onResult(false, "Insufficient balance")
            return
        }
        if (amount <= 0) {
            onResult(false, "Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            try {
                val request = TransferFundsRequest(
                    sender_id = getUserId(),
                    receiver_id = recipient,
                    amount = amount
                )
                val response = SupabaseManager.client.postgrest.rpc(
                    "transfer_funds",
                    request
                ).decodeAs<TransferFundsResponse>()

                if (response.success) {
                    fetchData()
                    onResult(true, "Transfer successful")
                } else {
                    Log.e("AppViewModel", "RPC Error: ${response.error}")
                    onResult(false, response.message ?: response.error ?: "Transfer failed")
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error sending money", e)
                onResult(false, e.message ?: "An error occurred")
            }
        }
    }

    fun sendMessage(conversationId: String, text: String, isAi: Boolean = false) {
        viewModelScope.launch {
            try {
                val msg = Message(
                    id = UUID.randomUUID().toString(),
                    conversation_id = conversationId,
                    text = text,
                    is_from_me = true,
                    is_ai = isAi,
                    timestamp = System.currentTimeMillis()
                )
                SupabaseManager.client.postgrest["messages"].insert(msg)
                
                val flow = messageFlows[conversationId]
                if (flow != null) {
                    flow.value = listOf(msg) + flow.value
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error sending message", e)
            }
        }
    }

    fun receiveMessage(conversationId: String, text: String, isAi: Boolean = false) {
        viewModelScope.launch {
            try {
                val msg = Message(
                    id = UUID.randomUUID().toString(),
                    conversation_id = conversationId,
                    text = text,
                    is_from_me = false,
                    is_ai = isAi,
                    timestamp = System.currentTimeMillis()
                )
                SupabaseManager.client.postgrest["messages"].insert(msg)
                
                val flow = messageFlows[conversationId]
                if (flow != null) {
                    flow.value = listOf(msg) + flow.value
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error receiving message", e)
            }
        }
    }

    fun updateUserProfile(name: String, bio: String) {
        val currentProfile = _userProfile.value ?: Profile(getUserId(), "", "")
        viewModelScope.launch {
            try {
                val newProfile = currentProfile.copy(display_name = name, bio = bio)
                SupabaseManager.client.postgrest["profiles"]
                    .update(
                        {
                            set("display_name", newProfile.display_name)
                            set("bio", newProfile.bio)
                        }
                    ) { filter { eq("id", getUserId()) } }
                _userProfile.value = newProfile
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error updating profile", e)
            }
        }
    }
}
