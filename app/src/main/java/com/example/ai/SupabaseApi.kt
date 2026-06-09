package com.example.ai

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val access_token: String? = null,
    val user: SupabaseUser? = null,
    val error_description: String? = null,
    val msg: String? = null
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String? = null
)

@Serializable
data class Profile(
    val id: String,
    val display_name: String,
    val bio: String
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
    val timestamp: Long
)

@Serializable
data class Message(
    val id: String = "",
    val conversation_id: String,
    val text: String,
    val is_from_me: Boolean,
    val is_ai: Boolean,
    val timestamp: Long
)

interface SupabaseApiService {
    @POST("auth/v1/signup")
    suspend fun signup(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): AuthResponse

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): AuthResponse

    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") idEq: String
    ): List<Profile>

    @POST("rest/v1/profiles")
    suspend fun insertProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body profile: Profile
    )

    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") idEq: String,
        @Body profile: Profile
    )

    @GET("rest/v1/wallets")
    suspend fun getWallet(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("user_id") userIdEq: String
    ): List<Wallet>

    @POST("rest/v1/wallets")
    suspend fun upsertWallet(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body wallet: Wallet
    )

    @GET("rest/v1/transactions")
    suspend fun getTransactions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("user_id") userIdEq: String,
        @Query("order") order: String = "timestamp.desc"
    ): List<Transaction>

    @POST("rest/v1/transactions")
    suspend fun insertTransaction(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Body transaction: Transaction
    )

    @GET("rest/v1/conversations")
    suspend fun getConversations(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("user_id") userIdEq: String,
        @Query("order") order: String = "timestamp.desc"
    ): List<Conversation>

    @GET("rest/v1/messages")
    suspend fun getMessages(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("conversation_id") convIdEq: String,
        @Query("order") order: String = "timestamp.desc"
    ): List<Message>

    @POST("rest/v1/messages")
    suspend fun insertMessage(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Body message: Message
    )
}

object SupabaseClient {
    private const val BASE_URL = "https://${BuildConfig.SUPABASE_PROJECT_ID}.supabase.co/"

    private val okHttpClient = OkHttpClient.Builder()
        .build()

    val service: SupabaseApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        // Ensure BuildConfig.SUPABASE_PROJECT_ID is not empty before building. If empty, use a dummy path to avoid crash
        val baseUrl = if (BuildConfig.SUPABASE_PROJECT_ID.isNotEmpty()) BASE_URL else "https://dummy.supabase.co/"
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(SupabaseApiService::class.java)
    }
    
    var currentUser: SupabaseUser? = null
    var currentToken: String? = null
}
