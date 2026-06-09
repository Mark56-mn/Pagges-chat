package com.example.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.ai.AuthRequest
import com.example.ai.SupabaseClient
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (BuildConfig.SUPABASE_PROJECT_ID.isEmpty() || BuildConfig.SUPABASE_API_KEY.isEmpty()) {
            onResult(false, "Supabase credentials missing from Secrets.")
            return
        }

        viewModelScope.launch {
            try {
                val response = SupabaseClient.service.login(
                    apiKey = BuildConfig.SUPABASE_API_KEY,
                    request = AuthRequest(email, password)
                )
                if (response.access_token != null && response.user != null) {
                    SupabaseClient.currentToken = response.access_token
                    SupabaseClient.currentUser = response.user
                    onResult(true, null)
                } else {
                    onResult(false, response.error_description ?: response.msg ?: "Login failed")
                }
            } catch (e: Exception) {
                Log.e("Auth", "Login error", e)
                onResult(false, e.message ?: "An error occurred")
            }
        }
    }

    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (BuildConfig.SUPABASE_PROJECT_ID.isEmpty() || BuildConfig.SUPABASE_API_KEY.isEmpty()) {
            onResult(false, "Supabase credentials missing from Secrets.")
            return
        }

        viewModelScope.launch {
            try {
                val response = SupabaseClient.service.signup(
                    apiKey = BuildConfig.SUPABASE_API_KEY,
                    request = AuthRequest(email, password)
                )
                if (response.access_token != null && response.user != null) {
                    SupabaseClient.currentToken = response.access_token
                    SupabaseClient.currentUser = response.user
                    onResult(true, null)
                } else if (response.user != null) {
                 	// sometimes signup doesn't return token if email confirmation is required
                    onResult(true, "Please check your email to confirm.")
                } else {
                    onResult(false, response.error_description ?: response.msg ?: "Signup failed")
                }
            } catch (e: Exception) {
                Log.e("Auth", "Signup error", e)
                onResult(false, e.message ?: "An error occurred")
            }
        }
    }

    fun logout() {
        SupabaseClient.currentToken = null
        SupabaseClient.currentUser = null
    }
}
