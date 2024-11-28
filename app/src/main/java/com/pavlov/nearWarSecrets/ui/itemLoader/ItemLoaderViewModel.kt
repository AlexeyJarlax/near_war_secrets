package com.pavlov.nearWarSecrets.ui.itemLoader

import android.app.Application
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.lifecycle.*
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.file.FileProviderAdapter
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

@HiltViewModel
class ItemLoaderViewModel @Inject constructor(
    application: Application,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _photoList = MutableLiveData<List<String>>()
    val photoList: LiveData<List<String>> get() = _photoList

    private val encryption = Encryption(context)
    private val apkManager = APKM(context)

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())

    private val _isPreviewVisible = MutableLiveData<Boolean>(false)
    val isPreviewVisible: LiveData<Boolean> get() = _isPreviewVisible

    private val _cameraSelector = MutableLiveData<CameraSelector>(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: LiveData<CameraSelector> get() = _cameraSelector

    private val _rotationAngle = MutableLiveData<Int>(0)
    val rotationAngle: LiveData<Int> get() = _rotationAngle

    private val _imageDialogVisible = MutableLiveData<Boolean>(false)
    val imageDialogVisible: LiveData<Boolean> get() = _imageDialogVisible

    private val _selectedImageUri = MutableLiveData<Uri?>(null)
    val selectedImageUri: LiveData<Uri?> get() = _selectedImageUri

    private val _imageDialogAcceptance = MutableLiveData<Boolean>(false)
    val imageDialogAcceptance: LiveData<Boolean> get() = _imageDialogAcceptance

    private val _isFrontCamera = MutableLiveData<Boolean>(false)
    val isFrontCamera: LiveData<Boolean> get() = _isFrontCamera

    private val _loadingIndicatorVisible = MutableLiveData<Boolean>(false)
    val loadingIndicatorVisible: LiveData<Boolean> get() = _loadingIndicatorVisible

    private val _showSaveDialog = MutableLiveData<Boolean>()
    val showSaveDialog: LiveData<Boolean> get() = _showSaveDialog

    init {
        loadSavedPhotos()
    }

    private fun loadSavedPhotos() {
        val savedFiles = getPreviouslySavedFiles()
        _photoList.postValue(savedFiles)
    }

    fun addPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            // Копируем файл в папку приложения
            val fileName = getFileName()
            val destinationFile = File(context.filesDir, fileName)
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(destinationFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                // Обновляем список фотографий
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
        return date.toString() // Вы можете отформатировать дату по своему усмотрению
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

    fun getEncryptionKeyName(fileName: String): String {
        return when {
            fileName.endsWith(".p1", ignoreCase = true) -> apkManager.getMastersSecret(APK.KEY_BIG_SECRET_NAME1)
            fileName.endsWith(".p2", ignoreCase = true) -> apkManager.getMastersSecret(APK.KEY_BIG_SECRET_NAME2)
            fileName.endsWith(".p3", ignoreCase = true) -> apkManager.getMastersSecret(APK.KEY_BIG_SECRET_NAME3)
            else -> ""
        }
    }

    fun shareImage(fileName: String) {
        val imageFile = File(context.filesDir, fileName)
        val uri = FileProviderAdapter.getUriForFile(context, imageFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_the_img)))
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

    fun savePhotoWithChoice(uri: Uri, encrypt: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = getFileName()
            val destinationFile = File(context.filesDir, fileName)

            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (encrypt) {
                    val encryptionKey = getEncryptionKeyName(fileName)
                    encryption.encryptImage(uri, fileName, encryptionKey)
                } else {
                    val outputStream = FileOutputStream(destinationFile)
                    inputStream?.copyTo(outputStream)
                    outputStream.close()
                }
                inputStream?.close()
                loadSavedPhotos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _showSaveDialog.value = false
    }
}