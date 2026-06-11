package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object MediaCompressor {
    fun compressImageToWebP(context: Context, uri: Uri): File? {
        try {
            var input = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(input)
            input?.close()
            if (bitmap == null) return null

            var quality = 90
            var byteArray: ByteArray
            
            do {
                val stream = ByteArrayOutputStream()
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                bitmap.compress(format, quality, stream)
                byteArray = stream.toByteArray()
                quality -= 10
            } while (byteArray.size > 500 * 1024 && quality > 10) // compress to max 500KB

            val compressedFile = File(context.cacheDir, "img_${UUID.randomUUID()}.webp")
            val fos = FileOutputStream(compressedFile)
            fos.write(byteArray)
            fos.flush()
            fos.close()

            return compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
