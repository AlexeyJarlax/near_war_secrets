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
import com.pavlov.MyShadowGallery.MyShadowGallery
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.SettingsActivity
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
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)

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
                    showToast(context, context.getString(R.string.error_del))
                    showToast(context, "${thisFileName}")
                }
            } else {
//                showToast(context, "Файл $thisFileName не существует")
                Log.d("=== ЛОГИ: ", "Файл $thisFileName не существует")
            }
        }

//        fun generateFileName(context: Context, isBoolean: Boolean, folder: File): String {
//            val adjectives = context.resources.getStringArray(R.array.name_util_adjectives)
//            val nouns = context.resources.getStringArray(R.array.name_util_nouns)
//
//            if (adjectives.isEmpty() || nouns.isEmpty()) {
//                Log.d("FileNameGeneration", "Empty arrays: adjectives=${adjectives.isEmpty()}, nouns=${nouns.isEmpty()}")
//                return "FallbackFileName.unknown"
//            }
//
//            val randomName = "${adjectives.random()}_${nouns.random()}"
//            var fileName = "${randomName}.unknown"
//
//            if (isBoolean) {
//                fileName = "${fileName.substringBeforeLast(".")}.k"
//            } else {
//                fileName = "${fileName.substringBeforeLast(".")}.o"
//            }
//
//            if (folder.exists() || folder.mkdirs()) {
//                var counter = 1
//                var file = File(folder, fileName)
//
//                Log.d("FileNameGeneration", "Generated FileName: $fileName")
//
//                while (file.exists()) {
//                    fileName = "${randomName}_$counter"
//                    if (isBoolean) {
//                        fileName = "${fileName}.k"
//                    } else {
//                        fileName = "${fileName}.o"
//                    }
//                    file = File(folder, fileName)
//                    counter++
//
//                    Log.d("FileNameGeneration", "Conflict! New FileName: $fileName")
//                }
//            }
//
//            if (context is SettingsActivity) {
//                return applyNamingOption(fileName, context.currentNamingOption)
//            } else {
//                // Обработка случая, если context не является экземпляром SettingsActivity
//                return fileName
//            }
//        }
//
//        private fun applyNamingOption(fileName: String, namingOption: SettingsActivity.NamingOption): String {
//            return when (namingOption) {
//                SettingsActivity.NamingOption.RUSSIAN_HEROIC -> "Русский героический_$fileName"
//                SettingsActivity.NamingOption.ENGLISH_HEROIC -> "English heroic_$fileName"
//                SettingsActivity.NamingOption.CHINESE_HERO -> "中国英雄_$fileName"
//                SettingsActivity.NamingOption.SPANISH_HEROIC -> "Heroico español_$fileName"
//                SettingsActivity.NamingOption.SYSTEM -> fileName
//            }
//        }

        private fun showToast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }


}