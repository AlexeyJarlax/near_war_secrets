package com.practicum.kototeka

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.practicum.kototeka.util.NameUtil
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class Encryption(private val context: Context, private val encryptionKey: String) {

    val itemLoaderActivity = context as ItemLoaderActivity
    private val photoList = ArrayList<String>()

    companion object {

        fun initialize() {
            Timber.plant(Timber.DebugTree()) // для логирования багов
        }
    }

    fun getPhotoList(): List<String> {
        return photoList
    }

    fun addPhotoToList(photoUri: Uri) {
        val fileName = photoUri.lastPathSegment ?: ""
        photoList.add(fileName)
    }

    fun fileSavingOperat(outputFile: File, fileName: String) {
        toast("функцию с галереей отложил")
//        var outputFile = outputFile
//        var fileName = fileName
//        Timber.d("=== из галереи: ${outputFile.name} === путь: ${outputFile.absolutePath}")
//        val randomName = "${NameUtil.adjectives.random()}\n${NameUtil.nouns.random()}"
//        fileName = "${randomName}.unknown"
//
//        if (encryptionKey.isNotEmpty()) {
//            fileName = fileName.substringBeforeLast(".")
//            fileName = "${fileName}.k"
//        } else {
//            fileName = fileName.substringBeforeLast(".")
//            fileName = "${fileName}.o"
//        }
//
//        val folder = context.getExternalFilesDir(null)
//        if (folder != null) {
//            var counter = 1
//            var file = File(folder, fileName)
//
//            while (file.exists()) {
//                fileName = "${fileName}_$counter"
//                file = File(folder, fileName)
//                counter++
//            }
//            outputFile = File(folder, fileName)
//            if (File(context.getExternalFilesDir(null), "${fileName}").exists()) {
//                Timber.d("=== файл из галереи в храналище уже существует, будет перезапись: ${fileName}k")
//                val existingFile = File(context.getExternalFilesDir(null), "${fileName}k")
//                existingFile.delete()
//                outputFile = File(context.getExternalFilesDir(null), "${fileName}")
//            }
//            Timber.d("=== из галереи этап 2: ${fileName} === путь: ${outputFile.absolutePath}")
//            if (encryptionKey.isNotEmpty()) {
//                createThumbnail(
//                    context,
//                    outputFile.toUri()
//                )
//                encryptImage(
//                    outputFile.toUri(),
//                    encryptionKey,
//                    fileName
//                )
//            } else {
//                toast("Изображение сохранено без шифрования")
//                addPhotoToList(outputFile.toUri())
//                itemLoaderActivity.notifyDSC()
//            }
//        }
//        toast("Ошибка сохранения изображения")
    }

fun encryptImage(imageUri: Uri, encryptionKey: String, fileName: String) {
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
    Timber.d("=== файл hashedKey: ${hashedKey}")
    val keySpec = SecretKeySpec(hashedKey, "AES")
    val cipher = Cipher.getInstance("AES")
    Timber.d("=== файл cipher: ${cipher}")
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

fun decryptImage(file: File, decryptionKey: String): Bitmap {
    Timber.d("=== Начало декодирования. файл file: ${file.name}")
    val encryptedBytes = file.readBytes()
    val messageDigest = MessageDigest.getInstance("SHA-256")
    Timber.d("=== файл messageDigest: ${messageDigest}")
    val hashedKey = messageDigest.digest(decryptionKey.toByteArray())
    Timber.d("=== файл hashedKey: $hashedKey")
    val keySpec = SecretKeySpec(hashedKey, "AES")
    val cipher = Cipher.getInstance("AES")
    Timber.d("=== файл cipher: ${cipher}")
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
    val requestOptions = RequestOptions().override(30, 30)

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
                    val thumbnailName =
                        saveThumbnailWithRandomFileName(context, resource, imageUri)
                    photoList.add(0, thumbnailName)
                    itemLoaderActivity.notifyDSC()
                    toast("Превью сохранено")
                    deleteOriginalImage(imageUri)
                } else {
                    toast("Ошибка: Не удалось сохранить превью")
                }
            }
            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
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
            for (file in files.reversed()) {
                if (file.extension != "kk") {
                    savedFiles.add(file.name)
                }
            }
        }
    }
    return savedFiles
}

fun toast(text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}
}
