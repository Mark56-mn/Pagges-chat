package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null

    fun startRecording(): Boolean {
        if (!hasPermission()) {
            requestPermission()
            return false
        }

        val outputFile = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.m4a")
        currentFilePath = outputFile.absolutePath

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64000)
            setOutputFile(currentFilePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    fun stopRecording(): String? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: RuntimeException) {
            // Stop might throw if called immediately after start
            File(currentFilePath ?: "").delete()
            currentFilePath = null
        }
        mediaRecorder = null
        return currentFilePath
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (context is Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                0
            )
        }
    }
}
