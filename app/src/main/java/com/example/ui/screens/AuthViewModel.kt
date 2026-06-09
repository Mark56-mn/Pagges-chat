package com.example.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SupabaseManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseManager.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("Auth", "Login error", e)
                onResult(false, e.message ?: "An error occurred")
            }
        }
    }

    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseManager.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("Auth", "Signup error", e)
                onResult(false, e.message ?: "An error occurred")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseManager.client.auth.signOut()
            } catch (e: Exception) {
                Log.e("Auth", "Logout error", e)
            }
        }
    }
}
