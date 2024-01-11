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
import com.pavlov.MyShadowGallery.R
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

        fun generateFileName(context: Application, boolean: Boolean, folder: File): String {
            val language = try {
                getLanguage(context)
            } catch (e: IllegalArgumentException) {
                // Если язык не поддерживается, используем русский
                e.printStackTrace()
                return generateFileNameForRussian(boolean, folder)
            }

            val adjectives = when (language) {
                "ru" -> NameUtil.adjectives
                "en" -> EnglishNameUtil.adjectives
                "es" -> SpanishNameUtil.adjectives
                "zh" -> ChineseNameUtil.adjectives
                else -> {
                    // Если язык не поддерживается, используем русский
                    return generateFileNameForRussian(boolean, folder)
                }
            }

            val nouns = when (language) {
                "ru" -> NameUtil.nouns
                "en" -> EnglishNameUtil.nouns
                "es" -> SpanishNameUtil.nouns
                "zh" -> ChineseNameUtil.nouns
                else -> {
                    // Если язык не поддерживается, используем русский
                    return generateFileNameForRussian(boolean, folder)
                }
            }

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

                var counter = 1
                var file = File(folder, fileName)

                // Проверяем существование файла с таким именем и добавляем суффикс, если нужно
                while (file.exists()) {
                    fileName = "${randomName}_$counter"

                    if (boolean) {
                        fileName = "${fileName}.k"
                    } else {
                        fileName = "${fileName}.o"
                    }

                    file = File(folder, fileName)
                    counter++
                }
            } else {
                // toast(context, "Ошибка: Не удалось получить папку для сохранения файла")
            }

            return fileName
        }

        private fun generateFileNameForRussian(boolean: Boolean, folder: File?): String {
            // Используем русские слова, если язык не поддерживается
            val adjectives = NameUtil.adjectives
            val nouns = NameUtil.nouns

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

                var counter = 1
                var file = File(folder, fileName)

                // Проверяем существование файла с таким именем и добавляем суффикс, если нужно
                while (file.exists()) {
                    fileName = "${randomName}_$counter"

                    if (boolean) {
                        fileName = "${fileName}.k"
                    } else {
                        fileName = "${fileName}.o"
                    }

                    file = File(folder, fileName)
                    counter++
                }
            } else {
                // toast(context, "Ошибка: Не удалось получить папку для сохранения файла")
            }

            return fileName
        }

        fun getLanguage(context: Context): String {
            val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }

            return currentLocale.language
        }

        private fun showToast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }


}