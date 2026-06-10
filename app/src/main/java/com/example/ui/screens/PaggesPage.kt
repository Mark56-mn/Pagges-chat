package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.util.NetworkMonitor

@Composable
fun PaggesScreen() {
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle(initialValue = true)

    if (isConnected) {
        PaggesContent()
    } else {
        OfflineScreen()
    }
}

@Composable
fun PaggesContent() {
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(10) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Placeholder Item #$index",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineScreen() {
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.mario_run))
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            val composition by compositionResult
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No internet connection. Please check your network.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { /* Retry triggered instantly by flow */ }) {
                Text("Retry")
            }
        }
    }
}
