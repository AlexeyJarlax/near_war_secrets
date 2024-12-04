package com.pavlov.nearWarSecrets.ui.Images.extracted

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun saveImageToPrivateStorage(context: Context, uri: Uri): Boolean {
    return try {
        // Получаем входной поток из URI
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            // Создаем директорию внутри внутреннего хранилища приложения
            val imagesDir = File(context.filesDir, "ExtractedImages")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            // Создаем файл для сохранения
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(imagesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                stream.copyTo(outputStream)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
