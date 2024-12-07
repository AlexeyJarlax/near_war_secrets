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
import com.pavlov.nearWarSecrets.util.APK.RECEIVED_FROM_OUTSIDE
import com.pavlov.nearWarSecrets.util.APK.TEMP_IMAGES
import com.pavlov.nearWarSecrets.util.APK.UPLOADED_BY_ME
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _receivedfromoutside = MutableLiveData<List<Uri>>()
    val receivedfromoutside: LiveData<List<Uri>> = _receivedfromoutside

    private val _savedImages = MutableLiveData<List<Uri>>()
    val savedImages: LiveData<List<Uri>> = _savedImages

    private val _uploadedbyme = MutableLiveData<List<String>>()
    val uploadedbyme: LiveData<List<String>> = _uploadedbyme

    private val _anImageWasSharedWithUsNow = MutableStateFlow(false)
    val anImageWasSharedWithUsNow: StateFlow<Boolean> = _anImageWasSharedWithUsNow

    init {
        Timber.tag(TAG).d("=== Инициализация ImagesViewModel")
        loadExtractedImages()
        loadReceivedfromoutsideImages()
        loadUploadedbymeImages()
    }

    fun setAnImageWasSharedWithUsNow(value: Boolean) {
        _anImageWasSharedWithUsNow.value = value
        Timber.tag(TAG).d("=== Установлено _anImageWasSharedWithUsNow = $value")
    }

    fun dontSave() {
        _showSaveDialog.value = false
        Timber.tag(TAG).d("=== Диалог сохранения скрыт")
    }

    /** --------------------------------------------------ЛОАДЕРЫ СПИСКОВ--------------------------------------------------*/
    private suspend fun <T> loadFiles(
        directoryName: String,
        mapper: (File) -> T?,
        liveData: MutableLiveData<List<T>>
    ) {
        try {
            val directory = File(context.filesDir, directoryName)
            if (!directory.exists()) {
                val created = directory.mkdirs()
                if (created) {
                    Timber.tag(TAG).d("=== Директория $directoryName создана.")
                } else {
                    Timber.tag(TAG).e("=== Не удалось создать директорию $directoryName.")
                }
            } else {
                Timber.tag(TAG).d("=== Директория $directoryName уже существует.")
            }

            val files = directory.listFiles()?.mapNotNull { file ->
                try {
                    mapper(file)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "=== Ошибка при преобразовании файла: ${file.absolutePath}")
                    null
                }
            } ?: emptyList()

            liveData.postValue(files)
            Timber.tag(TAG).d("=== Загружено ${files.size} элементов из $directoryName.")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "=== Ошибка при загрузке файлов из $directoryName.")
        }
    }

    private fun loadExtractedImages() {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.tag(TAG).d("=== Загрузка временных изображений (TEMP_IMAGES)")
            loadFiles(TEMP_IMAGES, { file ->
                val uri = getFileUri(file.name)
                if (uri != null) {
                    uri
                } else {
                    Timber.tag(TAG).e("=== Не удалось получить URI для файла: ${file.absolutePath}")
                    null
                }
            }, _receivedfromoutside)
        }
    }

    private fun loadReceivedfromoutsideImages() {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.tag(TAG).d("=== Загрузка сохранённых изображений (RECEIVED_FROM_OUTSIDE)")
            loadFiles(RECEIVED_FROM_OUTSIDE, { file ->
                val uri = getFileUri(file.name)
                if (uri != null) {
                    uri
                } else {
                    Timber.tag(TAG).e("=== Не удалось получить URI для файла: ${file.absolutePath}")
                    null
                }
            }, _savedImages)
        }
    }

    private fun loadUploadedbymeImages() {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.tag(TAG).d("=== Загрузка загруженных пользователем изображений (UPLOADED_BY_ME)")
            try {
                val directory = File(context.filesDir, UPLOADED_BY_ME)
                if (!directory.exists()) {
                    val created = directory.mkdirs()
                    if (created) {
                        Timber.tag(TAG).d("=== Директория $UPLOADED_BY_ME создана.")
                    } else {
                        Timber.tag(TAG).e("=== Не удалось создать директорию $UPLOADED_BY_ME.")
                    }
                } else {
                    Timber.tag(TAG).d("=== Директория $UPLOADED_BY_ME уже существует.")
                }

                val files = directory.listFiles()?.mapNotNull { file ->
                    try {
                        file.name
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "=== Ошибка при получении имени файла: ${file.absolutePath}")
                        null
                    }
                } ?: emptyList()

                _uploadedbyme.postValue(files)
                Timber.tag(TAG).d("=== Загружено ${files.size} файлов в $UPLOADED_BY_ME.")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при загрузке файлов из $UPLOADED_BY_ME.")
            }
        }
    }

    /** ---------------------------------------------------- ЭДАРЫ -----------------------------------------------------------*/

    fun getFileUri(fileName: String): Uri? { // Возвращает content:// URI
        Timber.tag(TAG).d("=== Получение URI для файла: $fileName")
        val uploadedbymeDir = File(context.filesDir, UPLOADED_BY_ME)
        val receivedfromoutsideDir = File(context.filesDir, RECEIVED_FROM_OUTSIDE)
        val tempimagesDir = File(context.filesDir, TEMP_IMAGES)

        val file = when {
            File(uploadedbymeDir, fileName).exists() -> File(uploadedbymeDir, fileName)
            File(receivedfromoutsideDir, fileName).exists() -> File(receivedfromoutsideDir, fileName)
            File(tempimagesDir, fileName).exists() -> File(tempimagesDir, fileName)
            else -> {
                Timber.tag(TAG).e("=== Файл не найден: $fileName в известных директориях.")
                null
            }
        }

        return try {
            if (file != null && file.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                Timber.tag(TAG).d("=== Получен URI: $uri для файла: ${file.absolutePath}")
                uri
            } else {
                Timber.tag(TAG).e("=== Файл не существует: ${file?.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "=== Ошибка при получении URI для файла: $fileName")
            null
        }
    }

    fun addReceivedPhoto(uri: Uri) {
        Timber.tag(TAG).d("=== Добавление полученного изображения: $uri")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tempDir = File(context.filesDir, TEMP_IMAGES)
                if (!tempDir.exists()) {
                    val created = tempDir.mkdirs()
                    if (created) {
                        Timber.tag(TAG).d("=== Директория $TEMP_IMAGES создана.")
                    } else {
                        Timber.tag(TAG).e("=== Не удалось создать директорию $TEMP_IMAGES.")
                        return@launch
                    }
                }

                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Timber.tag(TAG).e("=== Не удалось открыть InputStream для URI: $uri")
                    return@launch
                }

                val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
                val file = File(tempDir, fileName)
                try {
                    file.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    Timber.tag(TAG).d("=== Временное изображение добавлено: ${file.absolutePath}")
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "=== Ошибка при копировании данных в файл: ${file.absolutePath}")
                } finally {
                    inputStream.close()
                }

                loadExtractedImages()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при добавлении полученного изображения: $uri")
            }
        }
    }

    fun addReceivedPhotos(uris: List<Uri>) {   // (для нескольких URI)
        Timber.tag(TAG).d("=== Добавление нескольких полученных изображений: ${uris.size} шт.")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tempDir = File(context.filesDir, TEMP_IMAGES)
                if (!tempDir.exists()) {
                    val created = tempDir.mkdirs()
                    if (created) {
                        Timber.tag(TAG).d("=== Директория $TEMP_IMAGES создана.")
                    } else {
                        Timber.tag(TAG).e("=== Не удалось создать директорию $TEMP_IMAGES.")
                        return@launch
                    }
                }

                uris.forEach { uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream == null) {
                            Timber.tag(TAG).e("=== Не удалось открыть InputStream для URI: $uri")
                            return@forEach
                        }

                        val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
                        val file = File(tempDir, fileName)
                        try {
                            file.outputStream().use { output ->
                                inputStream.copyTo(output)
                            }
                            Timber.tag(TAG).d("=== Временное изображение добавлено: ${file.absolutePath}")
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "=== Ошибка при копировании данных в файл: ${file.absolutePath}")
                        } finally {
                            inputStream.close()
                        }
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "=== Ошибка при обработке URI: $uri")
                    }
                }

                loadExtractedImages()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при добавлении нескольких полученных изображений.")
            }
        }
    }

    fun addPhoto(uri: Uri) { // для фоток из галереи
        Timber.tag(TAG).d("=== Добавление фотографии из галереи: $uri")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = getFileName()
                val uploadedbymeDir = File(context.filesDir, UPLOADED_BY_ME)
                if (!uploadedbymeDir.exists()) {
                    val created = uploadedbymeDir.mkdirs()
                    if (created) {
                        Timber.tag(TAG).d("=== Директория $UPLOADED_BY_ME создана.")
                    } else {
                        Timber.tag(TAG).e("=== Не удалось создать директорию $UPLOADED_BY_ME.")
                        return@launch
                    }
                }

                val destinationFile = File(uploadedbymeDir, fileName)
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        Timber.tag(TAG).e("=== Не удалось открыть InputStream для URI: $uri")
                        return@launch
                    }

                    val outputStream = FileOutputStream(destinationFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    Timber.tag(TAG).d("=== Фотография сохранена: ${destinationFile.absolutePath}")
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "=== Ошибка при копировании данных в файл: ${destinationFile.absolutePath}")
                }

                loadUploadedbymeImages()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при добавлении фотографии из галереи: $uri")
            }
        }
    }

    // Сохранение временного изображения в сохраненные
    fun saveExtractedImage(uri: Uri, whereTo: String): Boolean {
        Timber.tag(TAG).d("=== Сохранение временного изображения: $uri в $whereTo")
        return try {
            val savedDir = File(context.filesDir, whereTo)
            if (!savedDir.exists()) {
                val created = savedDir.mkdirs()
                if (created) {
                    Timber.tag(TAG).d("=== Директория $whereTo создана.")
                } else {
                    Timber.tag(TAG).e("=== Не удалось создать директорию $whereTo.")
                }
            }

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Timber.tag(TAG).e("=== Не удалось открыть InputStream для URI: $uri")
                return false
            }

            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(savedDir, fileName)
            try {
                file.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                Timber.tag(TAG).d("=== Изображение сохранено: ${file.absolutePath}")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при копировании данных в файл: ${file.absolutePath}")
                return false
            } finally {
                inputStream.close()
            }

            loadReceivedfromoutsideImages()
            removeExtractedImage(uri)
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "=== Ошибка при сохранении изображения: $uri")
            false
        }
    }

    /** ---------------------------------------------------- ДЕЛИТЕРЫ -----------------------------------------------------------*/
    fun uriToFile(uri: Uri): File? { // Например: content://com.pavlov.nearWarSecrets.fileprovider/uploadedbyme/image_...jpg
        Timber.tag(TAG).d("=== Преобразование URI в файл: $uri")
        val segments = uri.pathSegments
        return if (segments.size >= 2) {
            val dirName = segments[0]
            val fileName = segments[1]
            val directory = File(context.filesDir, dirName)
            val file = File(directory, fileName)
            if (file.exists()) {
                Timber.tag(TAG).d("=== Преобразованный файл: ${file.absolutePath}")
                file
            } else {
                Timber.tag(TAG).e("=== Файл не существует: ${file.absolutePath}")
                null
            }
        } else {
            Timber.tag(TAG).e("=== Неверный формат URI: $uri")
            null
        }
    }

    fun deletePhoto(fileUri: Uri) {
        Timber.tag(TAG).d("=== Удаление фотографии: $fileUri")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = uriToFile(fileUri)
                if (file != null && file.exists()) {
                    val deleted = file.delete()
                    if (deleted) {
                        Timber.tag(TAG).d("=== Файл удалён: ${file.absolutePath}")
                    } else {
                        Timber.tag(TAG).e("=== Не удалось удалить файл: ${file.absolutePath}")
                    }
                } else {
                    Timber.tag(TAG).e("=== Файл не найден для удаления: $fileUri")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при удалении файла: $fileUri")
            } finally {
                loadUploadedbymeImages()
            }
        }
    }

    fun removeExtractedImage(uri: Uri) {
        Timber.tag(TAG).d("=== Удаление временного изображения: $uri")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(uri.path ?: "")
                if (file.exists()) {
                    val deleted = file.delete()
                    if (deleted) {
                        Timber.tag(TAG).d("=== Временное изображение удалено: ${file.absolutePath}")
                    } else {
                        Timber.tag(TAG).e("=== Не удалось удалить временное изображение: ${file.absolutePath}")
                    }
                } else {
                    Timber.tag(TAG).e("=== Файл не найден для удаления: ${uri.path}")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при удалении временного изображения: $uri")
            } finally {
                loadExtractedImages()
            }
        }
    }

    // Очистка временных изображений
    fun clearExtractedImages() {
        Timber.tag(TAG).d("=== Очистка всех временных изображений")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tempDir = File(context.filesDir, TEMP_IMAGES)
                if (tempDir.exists()) {
                    tempDir.listFiles()?.forEach { file ->
                        try {
                            if (file.delete()) {
                                Timber.tag(TAG).d("=== Временное изображение удалено: ${file.absolutePath}")
                            } else {
                                Timber.tag(TAG).e("=== Не удалось удалить временное изображение: ${file.absolutePath}")
                            }
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "=== Ошибка при удалении файла: ${file.absolutePath}")
                        }
                    }
                } else {
                    Timber.tag(TAG).d("=== Директория $TEMP_IMAGES не существует.")
                }
                _receivedfromoutside.postValue(emptyList())
                Timber.tag(TAG).d("=== Все временные изображения очищены")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка при очистке временных изображений")
            }
        }
    }

    /** ---------------------------------------------------- ОБЩИЕ РАЗДЕЛЫ -----------------------------------------------------------*/

    fun getPhotoDate(fileName: String): String {
        Timber.tag(TAG).d("=== Получение даты для файла: $fileName")
        val photoListDir = File(context.filesDir, UPLOADED_BY_ME)
        val file = File(photoListDir, fileName)
        return if (file.exists()) {
            val date = Date(file.lastModified())
            Timber.tag(TAG).d("=== Дата файла $fileName: $date")
            date.toString()
        } else {
            Timber.tag(TAG).e("=== Файл не найден для получения даты: ${file.absolutePath}")
            "Неизвестно"
        }
    }

    fun getFileNameWithoutExtension(fileName: String): String {
        val name = fileName.substringBeforeLast('.')
        Timber.tag(TAG).d("=== Получено имя файла без расширения: $name из $fileName")
        return name
    }

    fun switchCamera() {
        val currentSelector = _cameraSelector.value
        _cameraSelector.value = if (currentSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        Timber.tag(TAG).d("=== Переключение камеры. Текущий селектор: $_cameraSelector")
    }

    fun getFileName(): String {
        val folder = context.filesDir
        val existOrNot = apkManager.getBoolean(APK.KEY_USE_THE_ENCRYPTION_K, false)
        val fileName = NamingStyleManager(context).generateFileName(existOrNot, folder)
        Timber.tag(TAG).d("=== Сгенерировано имя файла: $fileName")
        return fileName
    }

    fun onSavePhotoClicked(boolean: Boolean) {
        _showSaveDialog.value = boolean
        Timber.tag(TAG).d("=== Диалог сохранения отображён: $boolean")
    }

    // Стеганография

    fun shareImageWithHiddenOriginal(
        originalImageFile: File,
        memeResId: Int,
        onResult: (Uri?) -> Unit
    ) {
        Timber.tag(TAG).d("=== Начало стеганографии для файла: ${originalImageFile.absolutePath} с memeResId: $memeResId")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originalBitmap = BitmapFactory.decodeFile(originalImageFile.absolutePath)
                if (originalBitmap == null) {
                    Timber.tag(TAG).e("=== Не удалось декодировать оригинальное изображение из файла: ${originalImageFile.absolutePath}")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                val memeBitmap = BitmapFactory.decodeResource(context.resources, memeResId)
                if (memeBitmap == null) {
                    Timber.tag(TAG).e("=== Не удалось декодировать ресурс мем: $memeResId")
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

                Timber.tag(TAG).d("=== Изменённый размер оригинального бита: ${resizedOriginalBitmap.width}x${resizedOriginalBitmap.height}")

                val encodedBitmap = hideImageInMeme(memeBitmap, resizedOriginalBitmap)
                if (encodedBitmap == null) {
                    Timber.tag(TAG).e("=== Не удалось закодировать изображение")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                Timber.tag(TAG).d("=== Закодированное изображение размером: ${encodedBitmap.width}x${encodedBitmap.height}")

                val fileName = "meme_with_hidden_image_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(destinationFile)
                encodedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                Timber.tag(TAG).d("=== Закодированное изображение сохранено: ${destinationFile.absolutePath}")

                val uri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        destinationFile
                    )
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "=== Ошибка при получении URI для файла: ${destinationFile.absolutePath}")
                    null
                }

                if (uri != null) {
                    Timber.tag(TAG).d("=== Сгенерирован URI для закодированного изображения: $uri")
                } else {
                    Timber.tag(TAG).e("=== Не удалось сгенерировать URI для закодированного изображения")
                }

                withContext(Dispatchers.Main) {
                    onResult(uri)
                }

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "=== Ошибка во время стеганографии для файла: ${originalImageFile.absolutePath}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    private fun hideImageInMeme(memeBitmap: Bitmap, originalBitmap: Bitmap): Bitmap? {
        Timber.tag(TAG).d("=== Начало скрытия изображения в мем")
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
            Timber.tag(TAG).d("=== Скрытие изображения завершено")
            encodedBitmap
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "=== Ошибка в методе hideImageInMeme")
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


    private suspend fun extractOriginalImage(memeUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(memeUri)
                val memeBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (memeBitmap == null) {
                    Timber.e("=== Failed to decode meme image from URI: $memeUri")
                    return@withContext null
                }

                val originalBitmap = extractOriginalFromMeme(memeBitmap)

                if (originalBitmap == null) {
                    Timber.e("=== Failed to extract original image from meme.")
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
