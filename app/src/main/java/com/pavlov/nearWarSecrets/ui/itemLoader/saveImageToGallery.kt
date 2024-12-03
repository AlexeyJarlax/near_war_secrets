package com.pavlov.nearWarSecrets.ui.itemLoader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Функция для сохранения изображения в галерею.
 *
 * @param context Контекст приложения.
 * @param uri URI изображения, которое нужно сохранить.
 */
fun saveImageToGallery(context: Context, uri: Uri) {
    try {
        val saveIntent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(saveIntent)
        Toast.makeText(context, "Изображение сохранено в галерею.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Не удалось сохранить изображение.", Toast.LENGTH_SHORT).show()
    }
}