package com.example.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    viewModel: AppViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as Application)
    )
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val conversation = conversations.find { it.id == conversationId }
    val messagesFlow = remember(conversationId) { viewModel.getMessages(conversationId) }
    val messages by messagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var inputText by remember { mutableStateOf("") }
    var showAiModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = conversation?.other_party_name ?: "Chat",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAiModal = true }) {
                        Text("🤖", fontSize = 24.sp)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .imePadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(conversationId, inputText)
                            inputText = ""
                            // For demo purposes, auto-reply if they mention specific things, or just simple mock AI...
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                MessageBubble(
                    text = msg.text,
                    isFromMe = msg.is_from_me,
                    isAi = msg.is_ai
                )
            }
        }
    }

    if (showAiModal) {
        var aiInput by remember { mutableStateOf("") }
        var isAiLoading by remember { mutableStateOf(false) }
        val scope = androidx.compose.runtime.rememberCoroutineScope()

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAiModal = false },
            title = { Text("Ask AI Assistant") },
            text = {
                Column {
                    Text("I can answer basic questions about the app or give you fun facts!", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = aiInput,
                        onValueChange = { aiInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Write a fun message to Marcus") },
                        minLines = 3
                    )
                    if (isAiLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Thinking...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val prompt = aiInput
                        if (prompt.isNotBlank()) {
                            isAiLoading = true
                            scope.launch {
                                val reply = com.example.ai.askGemini(prompt)
                                viewModel.receiveMessage(conversationId, reply, isAi = true)
                                showAiModal = false
                            }
                        }
                    },
                    enabled = !isAiLoading && aiInput.isNotBlank()
                ) {
                    Text("Ask")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAiModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(text: String, isFromMe: Boolean, isAi: Boolean) {
    val bgColor = when {
        isAi -> Color(0xFFE8F5E9)   // Light green for AI
        isFromMe -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isAi -> Color(0xFF1B5E20)
        isFromMe -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bgColor)
                .padding(12.dp)
        ) {
            Text(text = text, color = textColor, fontSize = 16.sp)
        }
    }
}
