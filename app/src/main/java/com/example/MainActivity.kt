package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.data.local.UserPreferences
import com.example.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val networkMonitor = com.example.util.NetworkMonitor(this)
    val syncManager = com.example.worker.SyncManager(this)
    val userPreferences = UserPreferences(this)

    lifecycleScope.launch {
        var wasOffline = false
        networkMonitor.isOnline.collectLatest { isOnline ->
            if (isOnline && wasOffline) {
                syncManager.triggerSync()
            }
            if (!isOnline) {
                wasOffline = true
            }
        }
    }

    enableEdgeToEdge()
    setContent {
      val themeMode by userPreferences.themeMode.collectAsState(initial = "System")
      val darkTheme = when (themeMode) {
          "Light" -> false
          "Dark" -> true
          else -> isSystemInDarkTheme()
      }

      MyApplicationTheme(darkTheme = darkTheme) {
        var showSplash by rememberSaveable { mutableStateOf(true) }
        
        Crossfade(targetState = showSplash, label = "App Content Crossfade") { isSplash ->
            if (isSplash) {
                SplashScreen(onSplashFinished = { showSplash = false })
            } else {
                val navController = rememberNavController()
                val authViewModel: com.example.ui.screens.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                
                val isLogged = remember { 
                    com.example.data.SupabaseManager.client.pluginManager.getPluginOrNull(io.github.jan.supabase.gotrue.Auth)?.currentUserOrNull() != null 
                }
                val startDest = if (isLogged) "main" else "login"
                NavHost(navController = navController, startDestination = startDest) {
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
                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() }
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
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
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

