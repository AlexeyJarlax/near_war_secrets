package com.pavlov.MyShadowGallery.util
// класс для конвертирования форматов изображений
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileProviderAdapter {

    companion object {

        fun bitmapToFile(bitmap: Bitmap, context: Context, fileName: String): File {
            try {
                // Проверяем, существует ли файл с указанным именем
                val existingFile = File(context.filesDir, fileName)
                if (existingFile.exists()) {
                    // Файл уже существует, выдаем Toast и возвращаем существующий файл
                    toast(context, "Файл $fileName в хранилище")
                    return existingFile
                }

                // Создаем новый файл в каталоге filesDir с указанным именем
                val file = File(context.filesDir, fileName)

                // Создаем поток для записи в файл
                val stream = FileOutputStream(file)

                // Сжимаем изображение и записываем его в файл в формате PNG
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)

                // Закрываем поток
                stream.close()

                recycleBitmap(bitmap)

                // Возвращаем созданный файл
                return file

            } catch (e: IOException) {
                recycleBitmap(bitmap)
                e.printStackTrace()
                throw e // Пробросим исключение дальше, чтобы вызывающий код мог обработать ошибку
            }

        }

        fun getUriForFile(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }

        fun rotateBitmap(file: File, degrees: Int): Bitmap {
            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)

            // Уменьшаем размер изображения на 50%
            val compressedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * 1).toInt(),
                (originalBitmap.height * 1).toInt(),
                true
            )
            // Поворачиваем изображение
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            return Bitmap.createBitmap(
                compressedBitmap,
                0,
                0,
                compressedBitmap.width,
                compressedBitmap.height,
                matrix,
                true
            )

        }

        fun recycleBitmap(bitmap: Bitmap?) {
            bitmap?.let {
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
        }

        // Метод для удаления файла после bitmapToFile
        fun deleteFile(thisFileName: String, context: Context) {
            val fileToDelete = File(context.filesDir, thisFileName)

            if (fileToDelete.exists()) {
                val isDeleted = fileToDelete.delete()

                if (isDeleted) {
                    toast(context, "Файл успешно удален: $thisFileName")
                } else {
                    toast(context, "Ошибка при удалении файла: $thisFileName")
                }
            } else {
                toast(context, "Файл $thisFileName не существует")
            }
        }

        fun generateFileName(boolean: Boolean, folder: File): String {

            val randomName = "${NameUtil.adjectives.random()}_${NameUtil.nouns.random()}"
            var fileName = "${randomName}.unknown"

            if (boolean) {
                fileName = fileName.substringBeforeLast(".")
                fileName = "${fileName}.k"
            } else {
                fileName = fileName.substringBeforeLast(".")
                fileName = "${fileName}.o"
            }

            if (folder != null) {
                if (!folder.exists()) {
                    folder.mkdirs()
                }
            }

            if (folder != null) {
                var counter = 1
                var file = File(folder, fileName)

                while (file.exists()) {
                    fileName = "${fileName}_$counter"
                    file = File(folder, fileName)
                    counter++
                }
            } else {
//                toast(context, "Ошибка: Не удалось получить папку для сохранения файла")
            }

            return fileName
        }

        private fun toast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }


}