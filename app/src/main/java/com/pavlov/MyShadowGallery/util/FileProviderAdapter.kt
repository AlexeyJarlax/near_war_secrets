package com.pavlov.MyShadowGallery.util
// класс для конвертирования форматов изображений
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileProviderAdapter {

    companion object {

        suspend fun bitmapToFileByKorutin(bitmap: Bitmap, context: Context, fileName: String): File {
            return withContext(Dispatchers.IO) {
                try {
                    // Создаем новый файл в каталоге filesDir с указанным именем
                    val file = File(context.filesDir, fileName)

                    // Если файл существует, удаляем его перед созданием нового
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d("=== ЛОГИ: ", "Прежний файл удален")
                        } else {
                            Log.d("=== ЛОГИ: ", "Не удалось удалить прежний файл")
                        }
                    }

                    // Создаем поток для записи в файл
                    val stream = FileOutputStream(file)

                    // Сжимаем изображение и записываем его в файл в формате PNG
                    bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream)

                    // Закрываем поток
                    stream.close()

                    recycleBitmap(bitmap)

                    // Выводим сообщение о успешном преобразовании
                    Log.d("=== ЛОГИ: ", "Bitmap преобразован в файл")

                    // Возвращаем созданный файл
                    file
                } catch (e: IOException) {
                    recycleBitmap(bitmap)
                    e.printStackTrace()
                    // Пробросим исключение дальше, чтобы вызывающий код мог обработать ошибку
                    throw e
                }
            }
        }

        fun getUriForFile(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }

        suspend fun <T> rotateImageByKorutin(input: T, degrees: Int): Bitmap = withContext(Dispatchers.Default) {
            val originalBitmap: Bitmap = when (input) {
                is File -> BitmapFactory.decodeFile(input.absolutePath)
                is Bitmap -> input
                else -> throw IllegalArgumentException("Input must be a File or Bitmap")
            }

            // Поворачиваем изображение
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            return@withContext Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )
        }

//        fun rotateBitmap(file: File, degrees: Int): Bitmap {
//            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
//
//            // Уменьшаем размер изображения на 50%
//            val compressedBitmap = Bitmap.createScaledBitmap(
//                originalBitmap,
//                (originalBitmap.width * 1).toInt(),
//                (originalBitmap.height * 1).toInt(),
//                true
//            )
//            // Поворачиваем изображение
//            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
//            return Bitmap.createBitmap(
//                compressedBitmap,
//                0,
//                0,
//                compressedBitmap.width,
//                compressedBitmap.height,
//                matrix,
//                true
//            )
//        }

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
//                    showToast(context, "Файл успешно удален: $thisFileName")
                } else {
                    showToast(context, "Ошибка при удалении файла: $thisFileName")
                }
            } else {
                showToast(context, "Файл $thisFileName не существует")
            }
        }

        fun generateFileName(context: Application, boolean: Boolean, folder: File): String {
            val isRussian = isRussianLanguage(context)
            val adjectives = if (isRussian) NameUtil.adjectives else EnglishNameUtil.adjectives
            val nouns = if (isRussian) NameUtil.nouns else EnglishNameUtil.nouns

            val randomName = "${adjectives.random()}_${nouns.random()}"
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

        fun isRussianLanguage(context: Application): Boolean {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                context.resources.configuration.locale
            }
            return locale.language == "ru"
        }

        private fun showToast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }


}