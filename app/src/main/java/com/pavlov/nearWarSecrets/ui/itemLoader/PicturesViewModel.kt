package com.pavlov.nearWarSecrets.ui.itemLoader

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.pavlov.nearWarSecrets.file.NamingStyleManager
import com.pavlov.nearWarSecrets.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import javax.inject.Inject
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import timber.log.Timber

@HiltViewModel
class PicturesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _photoList = MutableLiveData<List<String>>()
    val photoList: LiveData<List<String>> get() = _photoList

    private val apkManager = APKM(context)

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _cameraSelector = MutableLiveData<CameraSelector>(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: LiveData<CameraSelector> get() = _cameraSelector

    private val _showSaveDialog = MutableLiveData<Boolean>()
    val showSaveDialog: LiveData<Boolean> get() = _showSaveDialog

    // LiveData для извлеченных изображений
    private val _extractedImages = MutableLiveData<List<Uri>>()
    val extractedImages: LiveData<List<Uri>> get() = _extractedImages

    init {
        loadSavedPhotos()
        Timber.d("=== init class ItemLoaderViewModel")
    }

    private fun loadSavedPhotos() {
        val savedFiles = getPreviouslySavedFiles()
        _photoList.postValue(savedFiles)
    }

    fun addPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = getFileName()
            val destinationFile = File(context.filesDir, fileName)
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(destinationFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                loadSavedPhotos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePhoto(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileToDelete = File(context.filesDir, fileName)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            }
            loadSavedPhotos()
        }
    }

    fun getPhotoDate(fileName: String): String {
        val file = File(context.filesDir, fileName)
        val date = Date(file.lastModified())
        return date.toString()
    }

    fun getFileNameWithoutExtension(fileName: String): String {
        return fileName.substringBeforeLast('.')
    }

    fun switchCamera() {
        val currentSelector = _cameraSelector.value
        _cameraSelector.value = if (currentSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
    }

    fun getFileName(): String {
        val folder = context.filesDir
        val existOrNot = apkManager.getBoolean(APK.KEY_USE_THE_ENCRYPTION_K, false)
        return NamingStyleManager(context).generateFileName(existOrNot, folder)
    }

    private fun getPreviouslySavedFiles(): List<String> {
        val savedFiles = mutableListOf<String>()
        val directory = context.filesDir
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                val sortedFiles = files
                    .filter { it.extension.isNotEmpty() && it.extension !in listOf("kk", "dat", "k") }
                    .sortedByDescending { it.lastModified() }
                savedFiles.addAll(sortedFiles.map { it.name })
            }
        }
        return savedFiles
    }

    fun onSavePhotoClicked(boolean: Boolean) {
        // Показываем диалог, когда пользователь нажимает сохранить
        _showSaveDialog.value = boolean
    }

    fun savePhotoWithChoice(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = getFileName()
            Timber.d("=== Generated file name: %s", fileName)
            val destinationFile = File(context.filesDir, fileName)

            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(destinationFile)
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()
                loadSavedPhotos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _showSaveDialog.value = false
    }

    fun dontSave() {
        _showSaveDialog.value = false
    }

    fun getFileUri(fileName: String): Uri? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            null
        }
    }

    // Стеганография

    fun shareImageWithHiddenOriginal(
        originalImageFile: File,
        memeResId: Int,
        onResult: (Uri?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.d("Starting steganography with memeResId: $memeResId")
                val originalBitmap = BitmapFactory.decodeFile(originalImageFile.absolutePath)
                if (originalBitmap == null) {
                    Timber.e("Failed to decode original image from file: ${originalImageFile.absolutePath}")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }
                val memeBitmap = BitmapFactory.decodeResource(context.resources, memeResId)
                if (memeBitmap == null) {
                    Timber.e("Failed to decode meme resource: $memeResId")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                // Изменяем размер оригинала до размера мема, если необходимо
                val resizedOriginalBitmap = if (originalBitmap.width > memeBitmap.width || originalBitmap.height > memeBitmap.height) {
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        memeBitmap.width,
                        memeBitmap.height,
                        true
                    )
                } else {
                    originalBitmap
                }

                Timber.d("Resized original bitmap: ${resizedOriginalBitmap.width}x${resizedOriginalBitmap.height}")

                val encodedBitmap = hideImageInMeme(memeBitmap, resizedOriginalBitmap)
                if (encodedBitmap == null) {
                    Timber.e("Failed to encode image")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                Timber.d("Encoded bitmap: ${encodedBitmap.width}x${encodedBitmap.height}")

                val fileName = "meme_with_hidden_image_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(destinationFile)
                encodedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                Timber.d("Saved encoded image to: ${destinationFile.absolutePath}")

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destinationFile)
                Timber.d("Generated URI for encoded image: $uri")

                withContext(Dispatchers.Main) {
                    onResult(uri)
                }

            } catch (e: Exception) {
                Timber.e(e, "Error during shareImageWithHiddenOriginal")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    private fun hideImageInMeme(memeBitmap: Bitmap, originalBitmap: Bitmap): Bitmap? {
        return try {
            val memeWidth = memeBitmap.width
            val memeHeight = memeBitmap.height
            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height

            val encodedBitmap = memeBitmap.copy(Bitmap.Config.ARGB_8888, true)

            for (y in 0 until memeHeight) {
                for (x in 0 until memeWidth) {
                    val memePixel = memeBitmap.getPixel(x, y)

                    if (x < originalWidth && y < originalHeight) {
                        val originalPixel = originalBitmap.getPixel(x, y)

                        val encodedPixel = encodePixel(memePixel, originalPixel)
                        encodedBitmap.setPixel(x, y, encodedPixel)
                    }
                }
            }
            encodedBitmap
        } catch (e: Exception) {
            Timber.e(e, "Error during hideImageInMeme")
            null
        }
    }

    private fun encodePixel(memePixel: Int, originalPixel: Int): Int {
        // Извлекаем компоненты пикселей мемчика и оригинала
        val memeRed = (memePixel shr 16) and 0xFF
        val memeGreen = (memePixel shr 8) and 0xFF
        val memeBlue = memePixel and 0xFF

        val originalRed = (originalPixel shr 16) and 0xFF
        val originalGreen = (originalPixel shr 8) and 0xFF
        val originalBlue = originalPixel and 0xFF

        // Скрываем оригинальные данные в младших 4 битах мемчика
        val encodedRed = (memeRed and 0xF0) or (originalRed shr 4)
        val encodedGreen = (memeGreen and 0xF0) or (originalGreen shr 4)
        val encodedBlue = (memeBlue and 0xF0) or (originalBlue shr 4)

        // Возвращаем новый пиксель с альфа-каналом 255
        return (0xFF shl 24) or (encodedRed shl 16) or (encodedGreen shl 8) or encodedBlue
    }

    // Функции для обработки входящих изображений

    fun addReceivedPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            extractOriginalImage(uri)
        }
    }

    fun addReceivedPhotos(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val extractedUris = mutableListOf<Uri>()
            for (uri in uris) {
                val extractedUri = extractOriginalImage(uri)
                if (extractedUri != null) {
                    extractedUris.add(extractedUri)
                }
            }
            // Обновляем LiveData
            withContext(Dispatchers.Main) {
                _extractedImages.value = (_extractedImages.value ?: emptyList()) + extractedUris
            }
        }
    }

    private suspend fun extractOriginalImage(memeUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(memeUri)
                val memeBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (memeBitmap == null) {
                    Timber.e("Failed to decode meme image from URI: $memeUri")
                    return@withContext null
                }

                val originalBitmap = extractOriginalFromMeme(memeBitmap)

                if (originalBitmap == null) {
                    Timber.e("Failed to extract original image from meme.")
                    return@withContext null
                }

                // Сохраняем оригинальное изображение в файл
                val fileName = "original_image_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(destinationFile)
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destinationFile)

                uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun extractOriginalFromMeme(memeBitmap: Bitmap): Bitmap? {
        val width = memeBitmap.width
        val height = memeBitmap.height

        val originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val memePixel = memeBitmap.getPixel(x, y)

                // Извлекаем нижние 4 бита каждого цветового компонента
                val encodedRed = (memePixel shr 16) and 0x0F
                val encodedGreen = (memePixel shr 8) and 0x0F
                val encodedBlue = memePixel and 0x0F

                // Восстанавливаем оригинальные компоненты
                val originalRed = encodedRed shl 4
                val originalGreen = encodedGreen shl 4
                val originalBlue = encodedBlue shl 4

                // Собираем пиксель с альфа-каналом 255
                val originalPixel = (0xFF shl 24) or (originalRed shl 16) or (originalGreen shl 8) or originalBlue

                originalBitmap.setPixel(x, y, originalPixel)
            }
        }

        return originalBitmap
    }

    fun clearExtractedImages() {
        _extractedImages.value = emptyList()
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 1001
    }
}