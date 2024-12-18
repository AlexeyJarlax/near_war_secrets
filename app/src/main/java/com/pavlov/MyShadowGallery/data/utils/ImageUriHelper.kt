package com.pavlov.MyShadowGallery.data.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.pavlov.MyShadowGallery.util.APK.RECEIVED_FROM_OUTSIDE
import com.pavlov.MyShadowGallery.util.APK.TEMP_IMAGES
import com.pavlov.MyShadowGallery.util.APK.UPLOADED_BY_ME
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ImageUriHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TAG = "ImageUriHelper"

    fun getFileUri(fileName: String): Uri? {
        Timber.d("Получение URI для файла: $fileName")
        val directories = listOf(
            UPLOADED_BY_ME,
            RECEIVED_FROM_OUTSIDE,
            TEMP_IMAGES
        )

        for (dirName in directories) {
            val file = File(context.filesDir, "$dirName/$fileName")
            if (file.exists()) {
                return try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    Timber.d("Получен URI: $uri для файла: ${file.absolutePath}")
                    uri
                } catch (e: Exception) {
                    Timber.e(e, "Ошибка при получении URI для файла: $fileName")
                    null
                }
            }
        }

        Timber.e("Файл не найден: $fileName в известных директориях.")
        return null
    }

    fun uriToFile(uri: Uri): File? {
        Timber.d("Преобразование URI в файл: $uri")
        return when (uri.scheme) {
            "content" -> {
                val segments = uri.pathSegments
                if (segments.size >= 2) {
                    val dirName = segments[0]
                    val fileName = segments[1]
                    val directory = File(context.filesDir, dirName)
                    val file = File(directory, fileName)
                    if (file.exists()) {
                        Timber.d("Преобразованный файл: ${file.absolutePath}")
                        file
                    } else {
                        Timber.e("Файл не существует: ${file.absolutePath}")
                        null
                    }
                } else {
                    Timber.e("Неверный формат URI: $uri")
                    null
                }
            }
            "file" -> {
                val file = File(uri.path ?: "")
                if (file.exists()) {
                    Timber.d("Преобразованный файл: ${file.absolutePath}")
                    file
                } else {
                    Timber.e("Файл не существует: ${file.absolutePath}")
                    null
                }
            }
            else -> {
                Timber.e("Неподдерживаемая схема URI: ${uri.scheme}")
                null
            }
        }
    }
}