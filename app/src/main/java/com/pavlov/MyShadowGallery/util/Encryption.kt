package com.pavlov.MyShadowGallery.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.pavlov.MyShadowGallery.ItemLoaderActivity
import com.pavlov.MyShadowGallery.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Encryption(private val context: Context) {
    val itemLoaderActivity = context as ItemLoaderActivity
    private val photoList = ArrayList<String>()

    companion object {

    }

    fun getPhotoList(): List<String> {
        return photoList
    }

    fun deleteFile(fileName: String) {
        // Удаляем файл из списка
        photoList.remove(fileName)

        // Здесь добавьте логику удаления файла с файловой системы
        // Например:
        val fileToDelete = File(context.filesDir, fileName)
        fileToDelete.delete()
    }

    fun addPhotoToList(int: Int, photoUri: Uri) {
        val fileName = photoUri.lastPathSegment ?: ""
        photoList.add(int, fileName)
    }

    fun canSaveFilesFromGallery(): Boolean {
        return true
        Log.d("=== Encryption", "=== canSaveFilesFromGallery: ${Boolean}")
    }

    fun encryptImage(imageUri: Uri, fileName: String, encryptionKey: String) {
        toast(context.getString(R.string.wait))

        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return
        val originalFile = File(imageUri.path)

        // Заменяем текущее расширение на ".kk"
        val baseName = fileName.substringBeforeLast(".")
        val newFileName = "$baseName.kk"

        var encryptedFile = File(context.applicationContext.filesDir, newFileName)

        // Удаляем существующий файл с таким же именем
        if (encryptedFile.exists()) {
            Log.d("=== Encryption", "=== файл $newFileName существует, будет перезапись")
            encryptedFile.delete()
        }

        val outputStream = FileOutputStream(encryptedFile)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedKey = messageDigest.digest(encryptionKey.toByteArray())
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            val encryptedBytes = cipher.update(buffer, 0, read)
            outputStream.write(encryptedBytes)
        }
        val encryptedBytes = cipher.doFinal()
        outputStream.write(encryptedBytes)

        inputStream.close()
        outputStream.flush()
        outputStream.close()

        if (encryptedFile.exists()) {
            Log.d("=== Encryption", "=== файл $newFileName сохранен")
        } else {
            Log.e("=== Encryption", "=== Ошибка сохранения зашифрованного файла")
            return
        }

        toast(context.getString(R.string.encrypted_save))
        toast(encryptedFile.name)

        // Удаление оригинального изображения
        val isOriginalFileDeleted = originalFile.delete()
        if (!isOriginalFileDeleted) {
            Log.e("=== Encryption", "=== Ошибка удаления оригинального файла")
        }
    }

    fun encryptBitmap(bitmap: Bitmap, encryptionKey: String) {
        toast(context.getString(R.string.wait))
        Log.d("=== Encryption", "=== готовится к шифрованию")

        // Преобразуем Bitmap в массив байт
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        // Хеширование ключа
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedKey = messageDigest.digest(encryptionKey.toByteArray())
        val keySpec = SecretKeySpec(hashedKey, "AES")

        // Шифрование
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encryptedBytes = cipher.doFinal(imageBytes)

        // Сохранение зашифрованного массива байт в файл
        val encryptedFile = File(context.applicationContext.filesDir, "encrypted_image.png")
        FileOutputStream(encryptedFile).use { fileOutputStream ->
            fileOutputStream.write(encryptedBytes)
        }

        // Проверка сохранения файла
        if (!encryptedFile.exists()) {
            Log.e("=== Encryption", "=== Ошибка сохранения зашифрованного файла")
            return
        }

        toast(context.getString(R.string.encrypted_save))
        toast(encryptedFile.name)
        Log.d("=== Encryption", "=== Зашифрованный файл сохранен: ${encryptedFile.name}")
        Log.d("=== Encryption", "=== Путь к зашифрованному файлу: ${encryptedFile.absolutePath}")

        // Не забываем закрыть потоки и освободить ресурсы, если это необходимо
        outputStream.close()
    }

    fun decryptImage(file: File, decryptionKey: String): Bitmap {
//        val decryptionKey = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET)
        Log.e("=== Encryption", "=== Начало декодирования. файл file: ${file.name}")

        val encryptedBytes = file.readBytes()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedKey = messageDigest.digest(decryptionKey.toByteArray())

        val keySpec = SecretKeySpec(hashedKey, "AES")
        val cipher = Cipher.getInstance("AES")
//        Log.e("=== Encryption", "=== cipher")

        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        val decryptedBitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)

        Log.e("=== Encryption", "=== Успешный конец декодирования. файл decryptedBitmap: ${decryptedBitmap}")
        toast(context.getString(R.string.decrypting))

        return decryptedBitmap
    }

    fun isDecryptable(file: File, decryptionKey: String): Boolean {
        return try {
//            val decryptionKey = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET)
            Log.e("=== Encryption", "=== Начало декодирования. файл file: ${file.name}")
            val encryptedBytes = file.readBytes()
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val hashedKey = messageDigest.digest(decryptionKey.toByteArray())
            val keySpec = SecretKeySpec(hashedKey, "AES")
            val cipher = Cipher.getInstance("AES")
//            Log.e("=== Encryption", "=== cipher")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            cipher.doFinal(encryptedBytes)
            Log.e("=== Encryption", "=== Успешный конец декодирования.")
            true
        } catch (e: Exception) {
            Log.e("=== Encryption", "=== Ошибка при декодировании файла: ${e.message}")
            false
        }
    }



    fun createThumbnail(context: Context, input: Any) {
        var imageUri: Uri? = null
        var file: File? = null

        // Проверяем тип входных данных и устанавливаем imageUri или file соответственно
        when (input) {
            is Uri -> imageUri = input
            is File -> file = input
        }

        if (imageUri == null && file == null) {
            // Неверный ввод, обработайте по необходимости
            return
        }

        // Остальной код остается прежним с небольшими изменениями
        var scaledNumber = APKM(context)
            .getIntFromSP(APK.KEY_PREVIEW_SIZE_SEEK_BAR)
            ?: APK.DEFAULT_PREVIEW_SIZE
        if (scaledNumber <= 0) {
            scaledNumber = 1
        }
        if (scaledNumber > 100) {
            scaledNumber = 100
        }

        val requestOptions = RequestOptions()
            .override(scaledNumber, scaledNumber)
            .diskCacheStrategy(DiskCacheStrategy.NONE)

        val loadRequest = if (imageUri != null) {
            Glide.with(context)
                .asBitmap()
                .load(imageUri)
        } else {
            Glide.with(context)
                .asBitmap()
                .load(file)
        }

        loadRequest
            .apply(requestOptions)
            .signature(ObjectKey(System.currentTimeMillis()))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val thumbnailName =
                        saveThumbnailWithRandomFileName(context, resource, imageUri, file)
                    if (thumbnailName.isNotEmpty()) {
                        photoList.add(0, thumbnailName)
                        itemLoaderActivity.notifyDSC()
                        Log.e("=== Encryption", "=== Превью сохранено")
                        if (imageUri != null) {
                            deleteOriginalImage(imageUri)
                        } else {
                            // Обработайте удаление файла при необходимости
                        }
                    } else {
                        toast(context.getString(R.string.error_save))
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // То же, что и раньше
                }
            })
    }

    private fun saveThumbnailWithRandomFileName(
        context: Context,
        thumbnail: Bitmap,
        imageUri: Uri? = null,
        file: File? = null
    ): String {
        val fileName = if (imageUri != null) {
            File(imageUri.path).name
        } else {
            file?.name ?: ""
        }

        val fileExtension = fileName.substringAfterLast(".")
        val previewFileName = if (fileExtension.isNotEmpty()) {
            val defaultKey: Int = APKM(context).getIntFromSP(APK.DEFAULT_KEY)

            val fileNameWithoutExtension = fileName.substringBeforeLast(".")

            // when для определения расширения в зависимости от defaultKey
            val extension = when (defaultKey) {
                1 -> "p1"
                2 -> "p2"
                3 -> "p3"
                else -> "p" // Значение по умолчанию, если defaultKey не соответствует ожидаемым значениям
            }
            "$fileNameWithoutExtension.$extension"
        } else {
            context.getString(R.string.timber_img)
        }
        val fileToSave = File(context.applicationContext.filesDir, previewFileName)

        try {
            val outputStream = FileOutputStream(fileToSave)
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return previewFileName
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getDefaultThumbnail(context: Context): Bitmap {
        // Здесь вы можете использовать свою иконку документа или другое дефолтное изображение
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_search)
        return drawable?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

    fun createMiniFile(encryptedFile: File, expansion: String, inSampleSize: Int): File? {
        Log.d("=== PhotoListAdapter", "=== Option 0 selected")
        toast(context.getString(R.string.download))

        // Определяем размеры изображения без загрузки в память
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(encryptedFile.absolutePath, this)
        }

        // Вычисляем inSampleSize
        options.inSampleSize = inSampleSize

        // Загружаем изображение в память с уменьшением размера
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(encryptedFile.absolutePath, options)

        // Создаем выходной файл
        val folder = context.applicationContext.filesDir
        val fileName = removeFileExtension(encryptedFile.name)
        val outputFile = File(folder, "$fileName$expansion")

        // Сжимаем изображение и сохраняем его в файл
        FileOutputStream(outputFile).use { output ->
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 10, output)
        }

        // Освобождаем ресурсы
        bitmap?.recycle()

        return if (outputFile.exists()) {
            Log.d("=== PhotoListAdapter", "=== Output file exists")
            val updatedUri = outputFile.toUri()
            Log.d("=== PhotoListAdapter", "=== Updated URI: $updatedUri")
            addPhotoToList(0, updatedUri)
            toast(context.getString(R.string.done))
            outputFile
        } else {
            Log.e("=== PhotoListAdapter", "=== Output file does not exist")
            toast(context.getString(R.string.error_save))
            null
        }
    }

    fun removeFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf(".")
        return if (lastDotIndex == -1) {
            fileName
        } else {
            fileName.substring(0, lastDotIndex)
        }
    }

    private fun deleteOriginalImage(imageUri: Uri) {
        val file = File(imageUri.path)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getPreviouslySavedFiles(): List<String> {
        val savedFiles = mutableListOf<String>()
        val directory = context.applicationContext.filesDir
        if (directory != null && directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                val sortedFiles = files
                    .filter { it.extension.isNotEmpty() && it.extension !in listOf("kk", "dat", "k") }
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
