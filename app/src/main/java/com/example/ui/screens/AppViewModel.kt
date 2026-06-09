package com.example.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.ai.*
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

    private fun getAuthHeader(): String {
        return "Bearer ${SupabaseClient.currentToken ?: ""}"
    }

    private fun getApiKey(): String {
        return BuildConfig.SUPABASE_API_KEY
    }

    private fun getUserId(): String {
        return SupabaseClient.currentUser?.id ?: ""
    }

    private fun fetchData() {
        if (SupabaseClient.currentToken == null || getUserId().isEmpty()) return
        
        viewModelScope.launch {
            try {
                // Fetch Profile
                val profiles = SupabaseClient.service.getProfile(getApiKey(), getAuthHeader(), "eq.${getUserId()}")
                if (profiles.isNotEmpty()) {
                    _userProfile.value = profiles.first()
                } else {
                    val newProfile = Profile(id = getUserId(), display_name = "New User", bio = "")
                    SupabaseClient.service.insertProfile(getApiKey(), getAuthHeader(), profile = newProfile)
                    _userProfile.value = newProfile
                }

                // Fetch Wallet
                val wallets = SupabaseClient.service.getWallet(getApiKey(), getAuthHeader(), "eq.${getUserId()}")
                if (wallets.isNotEmpty()) {
                    _wallet.value = wallets.first()
                } else {
                    val newWallet = Wallet(user_id = getUserId(), balance = 0.0)
                    SupabaseClient.service.upsertWallet(getApiKey(), getAuthHeader(), wallet = newWallet)
                    _wallet.value = newWallet
                }

                // Fetch Transactions
                _transactions.value = SupabaseClient.service.getTransactions(getApiKey(), getAuthHeader(), "eq.${getUserId()}")

                // Fetch Conversations
                _conversations.value = SupabaseClient.service.getConversations(getApiKey(), getAuthHeader(), "eq.${getUserId()}")

            } catch (e: Exception) {
                Log.e("AppViewModel", "Error fetching data", e)
            }
        }
    }

    fun getMessages(conversationId: String): StateFlow<List<Message>> {
        val flow = messageFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        viewModelScope.launch {
            try {
                flow.value = SupabaseClient.service.getMessages(getApiKey(), getAuthHeader(), "eq.$conversationId")
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error fetching messages", e)
            }
        }
        return flow.asStateFlow()
    }

    fun addFunds(amount: Double) {
        val currentWallet = _wallet.value ?: return
        viewModelScope.launch {
            try {
                val newWallet = currentWallet.copy(balance = currentWallet.balance + amount)
                SupabaseClient.service.upsertWallet(getApiKey(), getAuthHeader(), wallet = newWallet)
                _wallet.value = newWallet

                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    user_id = getUserId(),
                    amount = amount,
                    party_name = "Deposit",
                    type = "RECEIVED",
                    timestamp = System.currentTimeMillis()
                )
                SupabaseClient.service.insertTransaction(getApiKey(), getAuthHeader(), newTx)
                _transactions.value = listOf(newTx) + _transactions.value
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error adding funds", e)
            }
        }
    }

    fun sendMoney(amount: Double, recipient: String) {
        val currentWallet = _wallet.value ?: return
        if (currentWallet.balance < amount) return
        
        viewModelScope.launch {
            try {
                val newWallet = currentWallet.copy(balance = currentWallet.balance - amount)
                SupabaseClient.service.upsertWallet(getApiKey(), getAuthHeader(), wallet = newWallet)
                _wallet.value = newWallet

                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    user_id = getUserId(),
                    amount = amount,
                    party_name = recipient,
                    type = "SENT",
                    timestamp = System.currentTimeMillis()
                )
                SupabaseClient.service.insertTransaction(getApiKey(), getAuthHeader(), newTx)
                _transactions.value = listOf(newTx) + _transactions.value
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error sending money", e)
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
                SupabaseClient.service.insertMessage(getApiKey(), getAuthHeader(), msg)
                
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
                SupabaseClient.service.insertMessage(getApiKey(), getAuthHeader(), msg)
                
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
                SupabaseClient.service.updateProfile(getApiKey(), getAuthHeader(), "eq.${getUserId()}", newProfile)
                _userProfile.value = newProfile
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error updating profile", e)
            }
        }
    }
}
