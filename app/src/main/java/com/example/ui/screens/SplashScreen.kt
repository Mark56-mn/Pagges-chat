package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentIndigo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onSplashFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentIndigo),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ChatBubble,
            contentDescription = "Pagges Chat Logo",
            tint = Color.White,
            modifier = Modifier.size(100.dp)
        )
    }
}
