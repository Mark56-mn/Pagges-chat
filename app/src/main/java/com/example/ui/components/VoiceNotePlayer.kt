package com.example.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.File
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VoiceNotePlayer(
    voiceNoteUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0) }
    var localPath by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Download/Cache voice note
    LaunchedEffect(voiceNoteUrl) {
        if (voiceNoteUrl.startsWith("http")) {
            withContext(Dispatchers.IO) {
                try {
                    val fileName = "voice_note_${voiceNoteUrl.hashCode()}.m4a"
                    val file = File(context.cacheDir, fileName)
                    if (!file.exists()) {
                        val input = URL(voiceNoteUrl).openStream()
                        file.outputStream().use { input.copyTo(it) }
                    }
                    localPath = file.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            localPath = voiceNoteUrl
        }
    }

    DisposableEffect(localPath) {
        if (localPath != null && File(localPath!!).exists()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(localPath)
                prepare()
                duration = this.duration
                setOnCompletionListener {
                    isPlaying = false
                    progress = 1f
                }
            }
        }
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            mediaPlayer?.start()
            while (isPlaying && mediaPlayer?.isPlaying == true) {
                progress = mediaPlayer!!.currentPosition.toFloat() / duration.toFloat().coerceAtLeast(1f)
                delay(50)
            }
        } else {
            mediaPlayer?.pause()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(8.dp)
            .widthIn(min = 200.dp, max = 250.dp)
    ) {
        IconButton(
            onClick = {
                if (progress >= 1f) mediaPlayer?.seekTo(0)
                isPlaying = !isPlaying 
            },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Slider(
            value = progress,
            onValueChange = {
                progress = it
                mediaPlayer?.seekTo((it * duration).toInt())
            },
            modifier = Modifier.weight(1f)
        )
    }
}
