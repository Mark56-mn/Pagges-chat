package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val currentTheme by userPreferences.themeMode.collectAsState(initial = "System")
    val syncWifi by userPreferences.syncOnlyOnWifi.collectAsState(initial = false)
    val autoDownload by userPreferences.autoDownloadMedia.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Theme Preference",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemeOption("Light", currentTheme) {
                coroutineScope.launch { userPreferences.saveThemeMode("Light") }
            }
            ThemeOption("Dark", currentTheme) {
                coroutineScope.launch { userPreferences.saveThemeMode("Dark") }
            }
            ThemeOption("System", currentTheme) {
                coroutineScope.launch { userPreferences.saveThemeMode("System") }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                text = "Data Usage",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sync only on Wi-Fi")
                Switch(
                    checked = syncWifi,
                    onCheckedChange = { chk -> coroutineScope.launch { userPreferences.setSyncOnlyOnWifi(chk) } }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-download media")
                Switch(
                    checked = autoDownload,
                    onCheckedChange = { chk -> coroutineScope.launch { userPreferences.setAutoDownloadMedia(chk) } }
                )
            }
        }
    }
}

@Composable
fun ThemeOption(title: String, selectedTheme: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = title == selectedTheme,
            onClick = null // handled by row
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}
