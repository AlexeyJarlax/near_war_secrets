package com.pavlov.MyShadowGallery.ui.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlov.MyShadowGallery.data.repository.ImageRepository
import com.pavlov.MyShadowGallery.domain.usecase.SteganographyUseCase
import com.pavlov.MyShadowGallery.util.APK.RECEIVED_FROM_OUTSIDE
import com.pavlov.MyShadowGallery.util.APK.UPLOADED_BY_ME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
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

    private val _encryptionProgress = MutableStateFlow<List<String>>(emptyList())
    val encryptionProgress: StateFlow<List<String>> = _encryptionProgress

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
            val fileName = "uploaded_${System.currentTimeMillis()}.jpg"
            imageRepository.addImage(uri, UPLOADED_BY_ME, fileName)
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
                val tempDir = File(imageRepository.context.filesDir, "TEMP_IMAGES")
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
                val tempDir = File(imageRepository.context.filesDir, "TEMP_IMAGES")
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

    fun saveSharedImage(uri: Uri, onExtractionResult: (Uri?) -> Unit) {
        val whereTo = RECEIVED_FROM_OUTSIDE
        Timber.tag(TAG).d("Сохранение временного изображения: $uri в $whereTo")
        try {
            val savedDir = File(imageRepository.context.filesDir, whereTo)
            if (!savedDir.exists()) {
                val created = savedDir.mkdirs()
                if (created) {
                    Timber.tag(TAG).d("Директория $whereTo создана.")
                } else {
                    Timber.tag(TAG).e("Не удалось создать директорию $whereTo.")
                }
            }

            val inputStream = imageRepository.context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Timber.tag(TAG).e("Не удалось открыть InputStream для URI: $uri")
                onExtractionResult(null)
                return
            }

            val fileName = imageRepository.getFileName()
            val file = File(savedDir, fileName)
            try {
                file.outputStream().use { output ->
                    inputStream.copyTo(output)
                    _anImageWasSharedWithUsNow.value = false
                }
                Timber.tag(TAG).d("Изображение сохранено: ${file.absolutePath}")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка при копировании данных в файл: ${file.absolutePath}")
                onExtractionResult(null)
                return
            } finally {
                inputStream.close()
            }

            viewModelScope.launch(Dispatchers.IO) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    val hiddenImageUri = steganographyUseCase.extractOriginalImage(uri)
                    if (hiddenImageUri != null) {
                        Timber.tag(TAG).d("Скрытое изображение извлечено: $hiddenImageUri")
                        withContext(Dispatchers.Main) {
                            onExtractionResult(hiddenImageUri)
                        }
                    } else {
                        Timber.tag(TAG).d("Скрытое изображение не найдено. Обработка как обычного изображения.")
                        withContext(Dispatchers.Main) {
                            onExtractionResult(uri)
                        }
                    }
                } else {
                    Timber.tag(TAG).e("Не удалось декодировать Bitmap из файла: ${file.absolutePath}")
                    withContext(Dispatchers.Main) {
                        onExtractionResult(null)
                    }
                }
                imageRepository.loadAllImages()
                imageRepository.clearTempImages()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Ошибка при сохранении изображения: $uri")
            onExtractionResult(null)
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
                _encryptionProgress.value = emptyList()
                _encryptionProgress.value = _encryptionProgress.value + "Сброс прогресса..."
                _encryptionProgress.value = _encryptionProgress.value + "Загрузка оригинального изображения..."
                val originalBitmap = BitmapFactory.decodeFile(originalImageFile.absolutePath)
                if (originalBitmap == null) {
                    Timber.tag(TAG).e("Не удалось декодировать оригинальное изображение из файла: ${originalImageFile.absolutePath}")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                _encryptionProgress.value = _encryptionProgress.value + "Загрузка изображения оболочки..."
                val memeBitmap = BitmapFactory.decodeResource(imageRepository.context.resources, memeResId)
                if (memeBitmap == null) {
                    Timber.tag(TAG).e("Не удалось декодировать ресурс мем: $memeResId")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                _encryptionProgress.value = _encryptionProgress.value + "Подгонка размеров изображений..."
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

                _encryptionProgress.value = _encryptionProgress.value + "Процесс шифрования..."
                val encodedBitmap = steganographyUseCase.hideImage(memeBitmap, resizedOriginalBitmap)
                if (encodedBitmap == null) {
                    Timber.tag(TAG).e("Не удалось закодировать изображение")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                Timber.tag(TAG).d("Закодированное изображение размером: ${encodedBitmap.width}x${encodedBitmap.height}")

                _encryptionProgress.value = _encryptionProgress.value + "Сохранение закодированного изображения..."
                val fileName = "meme_with_hidden_image_${System.currentTimeMillis()}.png"
                val destinationFile = File(imageRepository.context.cacheDir, fileName)
                val outputStream = FileOutputStream(destinationFile)
                encodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                Timber.tag(TAG).d("Закодированное изображение сохранено: ${destinationFile.absolutePath}")

                _encryptionProgress.value = _encryptionProgress.value + "Генерация URI"
                val uri = try {
                    FileProvider.getUriForFile(
                        imageRepository.context,
                        "${imageRepository.context.packageName}.fileprovider",
                        destinationFile
                    )
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Ошибка при получении URI для файла: ${destinationFile.absolutePath}")
                    null
                }

                if (uri != null) {
                    Timber.tag(TAG).d("Сгенерирован URI для закодированного изображения: $uri")
                } else {
                    Timber.tag(TAG).e("Не удалось сгенерировать URI для закодированного изображения")
                }

                withContext(Dispatchers.Main) {
                    onResult(uri)
                }

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка во время стеганографии для файла: ${originalImageFile.absolutePath}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    // Делегаты ImageRepository

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

    fun getFileName(): String {
        return imageRepository.getFileName()
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
