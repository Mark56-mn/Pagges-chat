package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var showSplash by rememberSaveable { mutableStateOf(true) }
        
        Crossfade(targetState = showSplash, label = "App Content Crossfade") { isSplash ->
            if (isSplash) {
                SplashScreen(onSplashFinished = { showSplash = false })
            } else {
                val navController = rememberNavController()
                val authViewModel: com.example.ui.screens.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        com.example.ui.screens.LoginScreen(
                            onNavigateToSignup = { navController.navigate("signup") },
                            onLoginSuccess = { 
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            viewModel = authViewModel
                        )
                    }
                    composable("signup") {
                        com.example.ui.screens.SignupScreen(
                            onNavigateToLogin = { navController.popBackStack() },
                            onSignupSuccess = { 
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            viewModel = authViewModel
                        )
                    }
                    composable("main") {
                        MainScreen(
                            onNavigateToChat = { chatId ->
                                navController.navigate("chat/$chatId")
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("chat/{chatId}") { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                        ChatDetailScreen(
                            conversationId = chatId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
      }
    }
  }
}

