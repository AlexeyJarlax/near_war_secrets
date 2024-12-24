package com.pavlov.MyShadowGallery.ui.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.MyShadowGallery.data.repository.ImageRepository
import com.pavlov.MyShadowGallery.data.repository.StegoEvent
import com.pavlov.MyShadowGallery.domain.usecase.SteganographyUseCase
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APK.RECEIVED_FROM_OUTSIDE
import com.pavlov.MyShadowGallery.util.APK.TEMP_IMAGES
import com.pavlov.MyShadowGallery.util.APK.UPLOADED_BY_ME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    val imageRepository: ImageRepository,
    private val steganographyUseCase: SteganographyUseCase,
) : ViewModel() {

    private val TAG = "ImagesViewModel"

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: StateFlow<CameraSelector> get() = _cameraSelector

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> get() = _showSaveDialog

    val receivedFromOutside = imageRepository.receivedFromOutside
    val tempImages = imageRepository.tempImages
    val uploadedByMe = imageRepository.uploadedByMe

    private val _anImageWasSharedWithUsNow = MutableStateFlow(false)
    val anImageWasSharedWithUsNow: StateFlow<Boolean> = _anImageWasSharedWithUsNow

    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> get() = _selectedUri

    private val _steganographyProgress = MutableStateFlow<List<String>>(emptyList())
    val steganographyProgress: StateFlow<List<String>> = _steganographyProgress

    private val _extractedUri = MutableStateFlow<Uri?>(null)
    val extractedUri: StateFlow<Uri?> get() = _extractedUri

    init {
        Timber.tag(TAG).d("Инициализация ImagesViewModel")
        loadAllImages()
    }

    private fun loadAllImages() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            imageRepository.loadAllImages()
            _isLoading.value = false
        }
    }

    fun setAnImageWasSharedWithUsNow(value: Boolean) {
        _anImageWasSharedWithUsNow.value = value
        Timber.tag(TAG).d("Установлено _anImageWasSharedWithUsNow = $value")
    }

    fun dontShow() {
        _showSaveDialog.value = false
        Timber.tag(TAG).d("Диалог сохранения скрыт")
    }

    fun setSelectedUri(uri: Uri?) {
        _selectedUri.value = uri
        Timber.tag(TAG).d("Установлено selectedUri: $uri")
    }

    fun clearSelectedUri() {
        _selectedUri.value = null
        Timber.tag(TAG).d("Очистка selectedUri")
    }

    fun switchCamera() {
        val currentSelector = _cameraSelector.value
        _cameraSelector.value = if (currentSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        Timber.tag(TAG).d("Переключение камеры. Текущий селектор: ${_cameraSelector.value}")
    }

    fun addPhoto(uri: Uri) {
        Timber.tag(TAG).d("Добавление фотографии: $uri")
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            imageRepository.addImage(uri, UPLOADED_BY_ME)
            _isLoading.value = false
        }
    }

    fun deletePhoto(uri: Uri) {
        Timber.tag(TAG).d("Удаление фотографии: $uri")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                imageRepository.deleteImage(uri)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка при удалении файла: $uri")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addReceivedPhoto(uri: Uri) {
        Timber.tag(TAG).d("Добавление полученного изображения: $uri")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tempDir = File(imageRepository.context.filesDir, TEMP_IMAGES)
                if (!tempDir.exists()) {
                    val created = tempDir.mkdirs()
                    if (created) {
                        Timber.tag(TAG).d("Директория TEMP_IMAGES создана.")
                    } else {
                        Timber.tag(TAG).e("Не удалось создать директорию TEMP_IMAGES.")
                        return@launch
                    }
                }

                val inputStream = imageRepository.context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Timber.tag(TAG).e("Не удалось открыть InputStream для URI: $uri")
                    return@launch
                }

                val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
                val file = File(tempDir, fileName)
                try {
                    file.outputStream().use { output ->
                        inputStream.copyTo(output)
                        _anImageWasSharedWithUsNow.value = true
                    }
                    Timber.tag(TAG).d("Временное изображение добавлено: ${file.absolutePath}")
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Ошибка при копировании данных в файл: ${file.absolutePath}")
                } finally {
                    inputStream.close()
                }

                loadAllImages()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка при добавлении полученного изображения: $uri")
            }
        }
    }

    fun addReceivedPhotos(uris: List<Uri>) {
        Timber.tag(TAG).d("Добавление нескольких полученных изображений: ${uris.size} шт.")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tempDir = File(imageRepository.context.filesDir, TEMP_IMAGES)
                if (!tempDir.exists()) {
                    val created = tempDir.mkdirs()
                    if (created) {
                        Timber.tag(TAG).d("Директория TEMP_IMAGES создана.")
                    } else {
                        Timber.tag(TAG).e("Не удалось создать директорию TEMP_IMAGES.")
                        return@launch
                    }
                }

                uris.forEach { uri ->
                    try {
                        val inputStream = imageRepository.context.contentResolver.openInputStream(uri)
                        if (inputStream == null) {
                            Timber.tag(TAG).e("Не удалось открыть InputStream для URI: $uri")
                            return@forEach
                        }

                        val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
                        val file = File(tempDir, fileName)
                        try {
                            file.outputStream().use { output ->
                                inputStream.copyTo(output)
                            }
                            Timber.tag(TAG).d("Временное изображение добавлено: ${file.absolutePath}")
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Ошибка при копировании данных в файл: ${file.absolutePath}")
                        } finally {
                            inputStream.close()
                        }
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Ошибка при обработке URI: $uri")
                    }
                }

                loadAllImages()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка при добавлении нескольких полученных изображений.")
            }
        }
    }

    fun saveBothImages(memeUri: Uri, extractedUri: Uri?, onSaveComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Сохранение мем-изображения
                imageRepository.addImage(memeUri, APK.RECEIVED_FROM_OUTSIDE)

                // Сохранение извлечённого изображения, если оно есть
                if (extractedUri != null) {
                    imageRepository.addImage(extractedUri, APK.RECEIVED_FROM_OUTSIDE)
                }

                // Извлечение оригинального изображения из мем-изображения
                steganographyUseCase.extractOriginalImage(memeUri)
                    .collect { event ->
                        when (event) {
                            is StegoEvent.Progress -> {
                                // Обработка прогресса
                                Timber.tag(TAG).d("Progress: ${event.message}")
                            }
                            is StegoEvent.Error -> {
                                // Обработка ошибки
                                Timber.tag(TAG).e("Error: ${event.message}")
                            }
                            is StegoEvent.Success -> {
                                // Успешное извлечение
                                Timber.tag(TAG).d("Extracted Image URI: ${event.uri}")
                                // Сохранение URI извлечённого изображения
                                event.uri?.let {
                                    imageRepository.addImage(it, APK.RECEIVED_FROM_OUTSIDE)
                                }
                            }
                        }
                    }

                // Удаление временных изображений перенесено
                // imageRepository.clearTempImages() // Удалено

                // Уведомление о завершении сохранения
                withContext(Dispatchers.Main) {
                    onSaveComplete()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка при сохранении обоих изображений")
            }
        }
    }

    fun shareImageWithHiddenOriginal(
        originalImageFile: File,
        memeResId: Int,
        onResult: (Uri?) -> Unit
    ) {
        Timber.tag(TAG).d("Начало стеганографии для файла: ${originalImageFile.absolutePath} с memeResId: $memeResId")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _steganographyProgress.value = emptyList()
                emitProgress("Сброс прогресса...")
                emitProgress("Загрузка оригинального изображения...")
                val originalBitmap = BitmapFactory.decodeFile(originalImageFile.absolutePath)
                if (originalBitmap == null) {
                    emitError("Не удалось декодировать оригинальное изображение из файла: ${originalImageFile.absolutePath}")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                emitProgress("Загрузка изображения оболочки...")
                val memeBitmap = BitmapFactory.decodeResource(imageRepository.context.resources, memeResId)
                if (memeBitmap == null) {
                    emitError("Не удалось декодировать ресурс мем: $memeResId")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                emitProgress("Подгонка размеров изображений...")
                val resizedOriginalBitmap =
                    if (originalBitmap.width > memeBitmap.width || originalBitmap.height > memeBitmap.height) {
                        Bitmap.createScaledBitmap(
                            originalBitmap,
                            memeBitmap.width,
                            memeBitmap.height,
                            true
                        )
                    } else {
                        originalBitmap
                    }

                Timber.tag(TAG).d("Изменённый размер оригинального изображения: ${resizedOriginalBitmap.width}x${resizedOriginalBitmap.height}")

                emitProgress("Процесс скрытия данных...")
                steganographyUseCase.hideImage(
                    memeBitmap,
                    resizedOriginalBitmap
                )
                    .onEach { event ->
                        when (event) {
                            is StegoEvent.Progress -> _steganographyProgress.value += event.message
                            is StegoEvent.Error -> {
                                _steganographyProgress.value += "Ошибка: ${event.message}"
                                withContext(Dispatchers.Main) {
                                    onResult(null)
                                }
                            }
                            is StegoEvent.Success -> {
                                Timber.tag(TAG).d("Закодированное изображение URI: ${event.uri}")
                                withContext(Dispatchers.Main) {
                                    onResult(event.uri)
                                }
                            }
                        }
                    }
                    .catch { e ->
                        Timber.tag(TAG).e(e, "Ошибка при скрытии данных в изображении")
                        emitError("Ошибка при скрытии данных: ${e.message}")
                        withContext(Dispatchers.Main) {
                            onResult(null)
                        }
                    }
                    .collect()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка во время стеганографии для файла: ${originalImageFile.absolutePath}")
                emitError("Ошибка во время стеганографии: ${e.message}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    // Функции для эмиссии прогресса и ошибок
    private fun emitProgress(message: String) {
        _steganographyProgress.value = _steganographyProgress.value + message
    }

    private fun emitError(message: String) {
        _steganographyProgress.value = _steganographyProgress.value + "Ошибка: $message"
    }

    fun clearExtractedUri() {
        _extractedUri.value = null
        Timber.tag(TAG).d("extractedUri очищен")
    }

    /** Делегаты ImageRepository */

    fun clearTempImages() {
        Timber.tag(TAG).d("Очистка всех временных изображений")
        viewModelScope.launch(Dispatchers.IO) {
            imageRepository.clearTempImages()
        }
    }

    fun uriToFile(uri: Uri): File? {
        return imageRepository.uriToFile(uri)
    }

    fun getPhotoDate(fileName: String): String {
        return imageRepository.getPhotoDate(fileName)
    }

    fun getFileName(directoryName: String): String {
        return imageRepository.getFileName(directoryName)
    }

    fun getFileNameWithoutExtension(fileName: String): String {
        return imageRepository.getFileNameWithoutExtension(fileName)
    }

    fun getFileUri(fileName: String): Uri? {
        return imageRepository.getFileUri(fileName)
    }

    fun onSavePhotoClicked(boolean: Boolean) {
        _showSaveDialog.value = boolean
        Timber.tag(TAG).d("Диалог сохранения отображён: $boolean")
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 1001
    }
}
