package com.pavlov.nearWarSecrets.ui.Images

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
import android.util.Log
import timber.log.Timber

@HiltViewModel
class ImagesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "ImagesViewModel"

    private val apkManager = APKM(context)

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _cameraSelector =
        MutableLiveData<CameraSelector>(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: LiveData<CameraSelector> get() = _cameraSelector

    private val _showSaveDialog = MutableLiveData<Boolean>()
    val showSaveDialog: LiveData<Boolean> get() = _showSaveDialog

    // LiveData для временных изображений (из поделиться)
    private val _extractedImages = MutableLiveData<List<Uri>>()
    val extractedImages: LiveData<List<Uri>> = _extractedImages

    // LiveData для сохраненных изображений (из поделиться)
    private val _savedImages = MutableLiveData<List<Uri>>()
    val savedImages: LiveData<List<Uri>> = _savedImages

    // LiveData для списка фотографий в LoaderScreen
    private val _photoList = MutableLiveData<List<String>>()
    val photoList: LiveData<List<String>> = _photoList

    init {
        loadExtractedImages()
        loadSavedImages()
        loadPhotoList()
        Log.d(TAG, "=== init class ImagesViewModel")
    }

    /** методы для работы функции приёма изображений от Поделиться*/

    // Загрузка временных изображений
    private fun loadExtractedImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val tempDir = File(context.filesDir, "TempImages")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            val images = tempDir.listFiles()?.map { Uri.fromFile(it) } ?: emptyList()
            _extractedImages.postValue(images)
            Log.d(TAG, "Загружено временных изображений: ${images.size}")
        }
    }

    // Загрузка сохраненных изображений
    private fun loadSavedImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedDir = File(context.filesDir, "ExtractedImages")
            if (!savedDir.exists()) {
                savedDir.mkdirs()
            }
            val images = savedDir.listFiles()?.map { Uri.fromFile(it) } ?: emptyList()
            _savedImages.postValue(images)
            Log.d(TAG, "Загружено сохраненных изображений: ${images.size}")
        }
    }

    // Добавление временных изображений
    fun addReceivedPhotos(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val tempDir = File(context.filesDir, "TempImages")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            uris.forEach { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.let { stream ->
                    val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
                    val file = File(tempDir, fileName)
                    file.outputStream().use { output ->
                        stream.copyTo(output)
                    }
                    Log.d(TAG, "Добавлено временное изображение: ${file.absolutePath}")
                }
            }
            loadExtractedImages()
        }
    }

    // Удаление временного изображения
    fun removeExtractedImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Удалено временное изображение: ${file.absolutePath}")
            } else {
                Log.e(TAG, "Файл не найден для удаления: ${uri.path}")
            }
            loadExtractedImages()
        }
    }

    // Сохранение временного изображения в сохраненные
    fun saveExtractedImage(uri: Uri): Boolean {
        return try {
            val savedDir = File(context.filesDir, "ExtractedImages")
            if (!savedDir.exists()) {
                savedDir.mkdirs()
            }
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(savedDir, fileName)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "Сохранено изображение: ${file.absolutePath}")
            // Обновляем список сохраненных изображений
            loadSavedImages()
            // Удаляем из временных
            removeExtractedImage(uri)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сохранении изображения: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // Очистка временных изображений
    fun clearExtractedImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val tempDir = File(context.filesDir, "TempImages")
            tempDir.listFiles()?.forEach {
                if (it.delete()) {
                    Log.d(TAG, "Удалено временное изображение: ${it.absolutePath}")
                } else {
                    Log.e(TAG, "Не удалось удалить временное изображение: ${it.absolutePath}")
                }
            }
            _extractedImages.postValue(emptyList())
            Timber.tag(TAG).d("Все временные изображения удалены")
        }
    }

// общие разделы

private fun loadPhotoList() {
    viewModelScope.launch(Dispatchers.IO) {
        val directory = File(context.filesDir, "PhotoList")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val files = directory.listFiles()?.map { it.name } ?: emptyList()
        _photoList.postValue(files)
        Log.d(TAG, "Загружено фото для LoaderScreen: ${files.size}")
    }
}

    fun addPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = getFileName()
            val photoListDir = File(context.filesDir, "PhotoList")
            if (!photoListDir.exists()) {
                photoListDir.mkdirs()
            }
            val destinationFile = File(photoListDir, fileName)
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(destinationFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                loadPhotoList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePhoto(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val photoListDir = File(context.filesDir, "PhotoList")
            val fileToDelete = File(photoListDir, fileName)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            }
            loadPhotoList() // Обновляем список после удаления
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

    fun onSavePhotoClicked(boolean: Boolean) {
        // Показываем диалог, когда пользователь нажимает сохранить
        _showSaveDialog.value = boolean
    }

    fun savePhotoWithChoice(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = getFileName()
            val destinationFile = File(context.filesDir, fileName)

            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(destinationFile)
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()
                loadPhotoList()
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

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destinationFile
                )
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
            Timber.e(e, "ошибка в методе hideImageInMeme")
            null
        }
    }

    private fun encodePixel(memePixel: Int, originalPixel: Int): Int {
        val memeRed = (memePixel shr 16) and 0xFF
        val memeGreen = (memePixel shr 8) and 0xFF
        val memeBlue = memePixel and 0xFF

        val originalRed = (originalPixel shr 16) and 0xFF
        val originalGreen = (originalPixel shr 8) and 0xFF
        val originalBlue = originalPixel and 0xFF

        val encodedRed = (memeRed and 0xF0) or (originalRed shr 4)
        val encodedGreen = (memeGreen and 0xF0) or (originalGreen shr 4)
        val encodedBlue = (memeBlue and 0xF0) or (originalBlue shr 4)

        return (0xFF shl 24) or (encodedRed shl 16) or (encodedGreen shl 8) or encodedBlue
    }

// Функции для обработки входящих изображений

    fun addReceivedPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            extractOriginalImage(uri)
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

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destinationFile
                )

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

                val encodedRed = (memePixel shr 16) and 0x0F
                val encodedGreen = (memePixel shr 8) and 0x0F
                val encodedBlue = memePixel and 0x0F

                val originalRed = encodedRed shl 4
                val originalGreen = encodedGreen shl 4
                val originalBlue = encodedBlue shl 4

                val originalPixel =
                    (0xFF shl 24) or (originalRed shl 16) or (originalGreen shl 8) or originalBlue

                originalBitmap.setPixel(x, y, originalPixel)
            }
        }

        return originalBitmap
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 1001
    }
}
