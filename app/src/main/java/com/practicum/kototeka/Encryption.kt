package com.practicum.kototeka

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import com.practicum.kototeka.util.AppPreferencesKeys
import com.practicum.kototeka.util.AppPreferencesKeysMethods


class Encryption(private val context: Context) {

    val itemLoaderActivity = context as ItemLoaderActivity
    private val photoList = ArrayList<String>()
//    private lateinit var sharedPreferences: SharedPreferences

    companion object {

        fun initialize() {
            Timber.plant(Timber.DebugTree()) // для логирования багов
        }
    }

    fun getPhotoList(): List<String> {
        return photoList
    }

    fun addPhotoToList(int : Int, photoUri: Uri) {
        val fileName = photoUri.lastPathSegment ?: ""
        photoList.add(int, fileName)
    }

    fun canSaveFilesFromGallery(): Boolean {
        return true
        Timber.d("canSaveFilesFromGallery: ${Boolean}")
    }

//    private fun getDecryptionKey(): String {
//        sharedPreferences = context.getSharedPreferences(AppPreferencesKeys.ENCRYPTION_KLUCHIK, Context.MODE_PRIVATE)
//        return sharedPreferences.getString(AppPreferencesKeys.ENCRYPTION_KLUCHIK, "") ?: ""
//    }

fun encryptImage(imageUri: Uri, fileName: String) {
//    val encryptionKey = getDecryptionKey()
    val encryptionKey = AppPreferencesKeysMethods(context).loadStringFromSharedPreferences(AppPreferencesKeys.ENCRYPTION_KLUCHIK)
    Timber.d("=== готовится к шифрованию, принимаем на вход fileName: ${fileName}")
    // Получаем путь к файлу, который нужно зашифровать
    val inputStream = context.contentResolver.openInputStream(imageUri) ?: return
    var encryptedFile = File(context.getExternalFilesDir(null), "${fileName}k")
    if (File(context.getExternalFilesDir(null), "${fileName}k").exists()) {
        Timber.d("=== файл fileName существует, будет перезапись: ${fileName}k")
        val existingFile = File(context.getExternalFilesDir(null), "${fileName}k")
        existingFile.delete()
    }
    Timber.d("=== готовится к шифрованию: ${encryptedFile.name}")
    Timber.d("=== путь к зашифрованному файлу: ${encryptedFile.absolutePath}")
    val outputStream = FileOutputStream(encryptedFile)
    val messageDigest = MessageDigest.getInstance("SHA-256")
    Timber.d("=== файл messageDigest: ${messageDigest}")
    val hashedKey = messageDigest.digest(encryptionKey.toByteArray())
    Timber.d("=== ключ: ${encryptionKey}")
    Timber.d("=== файл hashedKey: ${hashedKey}")
    val keySpec = SecretKeySpec(hashedKey, "AES")
    val cipher = Cipher.getInstance("AES")
    Timber.d("cipher")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec)

    val buffer = ByteArray(1024)
    var read: Int
    while (inputStream.read(buffer).also { read = it } != -1) {
        val encryptedBytes = cipher.update(buffer, 0, read)
        outputStream.write(encryptedBytes)
    }
    val encryptedBytes = cipher.doFinal()
    outputStream.write(encryptedBytes)

    if (File(context.getExternalFilesDir(null), fileName).exists()) {
        Timber.d("=== сейчас в директории существует файл fileName: ${fileName}")
    }
    if (File(context.getExternalFilesDir(null), "${fileName}k").exists()) {
        Timber.d("=== сейчас в директории существует файл {fileName}k: ${fileName}k")
    }

    val isEncryptedFileSaved = encryptedFile.exists()
    if (!isEncryptedFileSaved) {// Проверка на сохранение файла
        Timber.e("=== Ошибка сохранения зашифрованного файла")
        return
    }
    toast("Изображение зашифровано")
    Timber.d("=== Зашифрованный файл сохранен: ${encryptedFile.name}")
    Timber.d("=== Путь к зашифрованному файлу: ${encryptedFile.absolutePath}")
    //если задать имя fileName = "my_secret_photo.jpg", то файл будет сохранен в следующем виде:
    //storage/emulated/0/Android/data/[app_package_name]/files/my_secret_photo.jpg

    val originalFile = File(imageUri.path) // Удаление оригинального изображения
    val isOriginalFileDeleted = originalFile.delete()
    if (!isOriginalFileDeleted) { // Проверка на удаление оригинала
        Timber.e("=== Ошибка удаления оригинального файла")
        inputStream.close()        // Закрытие потоков
        outputStream.flush()
        outputStream.close()
    }
}

fun decryptImage(file: File): Bitmap {
//    val decryptionKey = getDecryptionKey()
    val decryptionKey = AppPreferencesKeysMethods(context).loadStringFromSharedPreferences(AppPreferencesKeys.ENCRYPTION_KLUCHIK)
    Timber.d("=== Начало декодирования. файл file: ${file.name}")
    val encryptedBytes = file.readBytes()
    val messageDigest = MessageDigest.getInstance("SHA-256")
    Timber.d("=== файл messageDigest: ${messageDigest}")
    val hashedKey = messageDigest.digest(decryptionKey.toByteArray())
    Timber.d("=== ключ: ${decryptionKey}")
    Timber.d("=== файл hashedKey: $hashedKey")
    val keySpec = SecretKeySpec(hashedKey, "AES")
    val cipher = Cipher.getInstance("AES")
    Timber.d("cipher")
    cipher.init(Cipher.DECRYPT_MODE, keySpec)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    val decryptedBitmap =
        BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
    val matrix = Matrix()
    matrix.postRotate(90f)
    val rotatedBitmap = Bitmap.createBitmap(
        decryptedBitmap,
        0,
        0,
        decryptedBitmap.width,
        decryptedBitmap.height,
        matrix,
        true
    )
    Timber.d("=== Успешный конец декодирования. файл rotatedBitmap: ${rotatedBitmap}")
    toast("Дешифрование ${file.name} выполнено успешно")
    return rotatedBitmap
}

    fun createThumbnail(context: Context, imageUri: Uri) {
        val scaledNumber = AppPreferencesKeysMethods(context)
            .loadPreviewSizeValue(AppPreferencesKeys.KEY_PREVIEW_SIZE_SEEK_BAR)
        val requestOptions = RequestOptions().override(scaledNumber, scaledNumber)

        Glide.with(context)
            .asBitmap()
            .load(imageUri)
            .apply(requestOptions)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val thumbnailName =
                        saveThumbnailWithRandomFileName(context, resource, imageUri)
                    if (thumbnailName.isNotEmpty()) {
                        photoList.add(0, thumbnailName)
                        itemLoaderActivity.notifyDSC()
                        toast("Превью сохранено")
                        deleteOriginalImage(imageUri)
                    } else {
                        toast("Ошибка: Не удалось сохранить превью")
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Для случая, когда изображение не загружено, например, если файл не является изображением
                    val defaultThumbnail = getDefaultThumbnail(context)
                    val defaultThumbnailName =
                        saveThumbnailWithRandomFileName(context, defaultThumbnail, imageUri)

                    if (defaultThumbnailName.isNotEmpty()) {
                        photoList.add(0, defaultThumbnailName)
                        itemLoaderActivity.notifyDSC()
                        toast("Превью сохранено (дефолтное изображение)")
                        deleteOriginalImage(imageUri)
                    } else {
                        toast("Ошибка: Не удалось сохранить превью (дефолтное изображение)")
                    }
                }
            })
    }

    private fun getDefaultThumbnail(context: Context): Bitmap {
        // Здесь вы можете использовать свою иконку документа или другое дефолтное изображение
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_search)
        return drawable?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

private fun saveThumbnailWithRandomFileName(
    context: Context,
    thumbnail: Bitmap,
    imageUri: Uri
): String {
    val fileName = File(imageUri.path).name

    val fileExtension = fileName.substringAfterLast(".")
    val previewFileName = if (fileExtension.isNotEmpty()) {
        val fileNameWithoutExtension = fileName.substringBeforeLast(".")
        "${fileNameWithoutExtension}.p"
    } else {
        "Пустая превьюшка"
    }
    val file = File(context.getExternalFilesDir(null), previewFileName)
    try {
        val outputStream = FileOutputStream(file)
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return previewFileName
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return ""
}

private fun deleteOriginalImage(imageUri: Uri) {
    val file = File(imageUri.path)
    if (file.exists()) {
        file.delete()
    }
}

    fun getPreviouslySavedFiles(): List<String> { // наполнение списка для RecyclerView
        val savedFiles = mutableListOf<String>()
        val directory = context.getExternalFilesDir(null)
        if (directory != null && directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                val sortedFiles = files
                    .filter { it.extension != "kk" }
                    .sortedBy { it.lastModified() } // Сортировка по времени создания в возрастающем порядке
                savedFiles.addAll(sortedFiles.map { it.name })
            }
        }
        return savedFiles
    }

fun toast(text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}
}
