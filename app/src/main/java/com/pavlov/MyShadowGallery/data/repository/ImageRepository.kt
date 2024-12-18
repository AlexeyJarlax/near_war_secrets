package com.pavlov.MyShadowGallery.data.repository

import android.content.Context
import android.net.Uri
import com.pavlov.MyShadowGallery.data.utils.ImageUriHelper
import com.pavlov.MyShadowGallery.file.NamingStyleManager
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class ImageRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val imageUriHelper: ImageUriHelper
) {

    private val TAG = "ImageRepository"
    private val apkManager = APKM(context)

    private val _receivedFromOutside = MutableStateFlow<List<String>>(emptyList())
    val receivedFromOutside: StateFlow<List<String>> = _receivedFromOutside

    private val _tempImages = MutableStateFlow<List<String>>(emptyList())
    val tempImages: StateFlow<List<String>> = _tempImages

    private val _uploadedByMe = MutableStateFlow<List<String>>(emptyList())
    val uploadedByMe: StateFlow<List<String>> = _uploadedByMe

    suspend fun loadImages(directoryName: String, stateFlow: MutableStateFlow<List<String>>) {
        try {
            val directory = File(context.filesDir, directoryName)
            if (!directory.exists()) {
                val created = directory.mkdirs()
                if (created) {
                    Timber.d("Директория $directoryName создана.")
                } else {
                    Timber.e("Не удалось создать директорию $directoryName.")
                }
            } else {
                Timber.d("Директория $directoryName уже существует.")
            }

            val files = directory.listFiles()?.mapNotNull { it.name } ?: emptyList()
            stateFlow.value = files
            Timber.d("Загружено ${files.size} элементов из $directoryName.")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке файлов из $directoryName.")
        }
    }

    suspend fun loadAllImages() {
        loadImages("TEMP_IMAGES", _tempImages)
        loadImages("RECEIVED_FROM_OUTSIDE", _receivedFromOutside)
        loadImages("UPLOADED_BY_ME", _uploadedByMe)
    }

    suspend fun addImage(uri: Uri, directoryName: String, fileName: String) {
        try {
            val dir = File(context.filesDir, directoryName)
            if (!dir.exists()) {
                val created = dir.mkdirs()
                if (created) {
                    Timber.d("Директория $directoryName создана.")
                } else {
                    Timber.e("Не удалось создать директорию $directoryName.")
                    return
                }
            }

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Timber.e("Не удалось открыть InputStream для URI: $uri")
                return
            }

            val file = File(dir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Timber.d("Изображение сохранено: ${file.absolutePath}")

            loadAllImages()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при добавлении изображения: $uri")
        }
    }

    suspend fun deleteImage(fileName: String, directoryName: String) {
        try {
            val file = File(context.filesDir, "$directoryName/$fileName")
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Timber.d("Файл удалён: ${file.absolutePath}")
                    loadAllImages()
                } else {
                    Timber.e("Не удалось удалить файл: ${file.absolutePath}")
                }
            } else {
                Timber.e("Файл не найден для удаления: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при удалении файла: $fileName")
        }
    }

    fun getFileUri(fileName: String): Uri? {
        return imageUriHelper.getFileUri(fileName)
    }

    fun uriToFile(uri: Uri): File? {
        return imageUriHelper.uriToFile(uri)
    }

    suspend fun clearTempImages() {
        try {
            val tempDir = File(context.filesDir, "TEMP_IMAGES")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    try {
                        if (file.delete()) {
                            Timber.d("Временное изображение удалено: ${file.absolutePath}")
                        } else {
                            Timber.e("Не удалось удалить временное изображение: ${file.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при удалении файла: ${file.absolutePath}")
                    }
                }
            }
            _tempImages.value = emptyList()
            Timber.d("Все временные изображения очищены")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при очистке временных изображений")
        }
    }

    fun getPhotoDate(fileName: String): String {
        Timber.d("Получение даты для файла: $fileName")
        val photoListDir = File(context.filesDir, "UPLOADED_BY_ME")
        val file = File(photoListDir, fileName)
        return if (file.exists()) {
            val date = java.util.Date(file.lastModified())
            Timber.d("Дата файла $fileName: $date")
            date.toString()
        } else {
            Timber.e("Файл не найден для получения даты: ${file.absolutePath}")
            "Неизвестно"
        }
    }

    fun getFileName(): String {
        val folder = context.filesDir
        val existOrNot = apkManager.getBoolean(APK.KEY_USE_THE_ENCRYPTION_K, false)
        val fileName = NamingStyleManager(context).generateFileName(existOrNot, folder)
        Timber.tag(TAG).d("=== Сгенерировано имя файла: $fileName")
        return fileName
    }

    fun getFileNameWithoutExtension(fileName: String): String {
        val name = fileName.substringBeforeLast('.')
        Timber.d("Получено имя файла без расширения: $name из $fileName")
        return name
    }
}
