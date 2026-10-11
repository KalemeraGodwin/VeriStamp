package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageUtil {
    fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String = "media"): String? {
        return try {
            val extension = context.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
            val dir = File(context.filesDir, "media").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(dir, "${prefix}_${System.currentTimeMillis()}.$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
