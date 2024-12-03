package com.pavlov.nearWarSecrets.ui.itemLoader

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.file.NamingStyleManager
import com.pavlov.nearWarSecrets.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import timber.log.Timber

@HiltViewModel
class ItemLoaderViewModel @Inject constructor(
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

    fun shareImage(fileName: String) {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            Timber.e("=== File not found: $file")
            return
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        Timber.d("=== Sharing file with URI: $uri")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            // Используем Activity контекст
            if (context is Activity) {
                context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            } else {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(Intent.createChooser(shareIntent, "Share Image").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            Timber.e("=== Error sharing image: $e")
        }
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

// Steganography

    fun shareImageWithHiddenOriginal(originalUri: Uri?) {
        if (originalUri == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originalInputStream = context.contentResolver.openInputStream(originalUri)
                val originalBitmap = BitmapFactory.decodeStream(originalInputStream)

                val memeBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.art)

                val encodedBitmap = hideImageInMeme(memeBitmap, originalBitmap)

                val fileName = "meme_with_hidden_image_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(destinationFile)
                encodedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

//                shareImage()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun hideImageInMeme(memeBitmap: Bitmap, originalBitmap: Bitmap): Bitmap {
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
        return encodedBitmap
    }

    private fun encodePixel(memePixel: Int, originalPixel: Int): Int {
        // Извлекаем компоненты пикселей мемчика и оригинала
        val memeRed = memePixel shr 16 and 0xFF
        val memeGreen = memePixel shr 8 and 0xFF
        val memeBlue = memePixel and 0xFF

        val originalRed = originalPixel shr 16 and 0xFF
        val originalGreen = originalPixel shr 8 and 0xFF
        val originalBlue = originalPixel and 0xFF

        // Скрываем оригинальные данные в младших битах мемчика
        val encodedRed = (memeRed and 0xF0) or (originalRed shr 4)
        val encodedGreen = (memeGreen and 0xF0) or (originalGreen shr 4)
        val encodedBlue = (memeBlue and 0xF0) or (originalBlue shr 4)

        // Возвращаем новый пиксель
        return (encodedRed shl 16) or (encodedGreen shl 8) or encodedBlue
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 1001
    }
}

