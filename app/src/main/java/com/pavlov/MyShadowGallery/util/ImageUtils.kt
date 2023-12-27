package com.pavlov.MyShadowGallery.util
// класс для конвертирования форматов изображений
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageUtils {
    companion object {
        fun bitmapToFile(bitmap: Bitmap, context: Context, fileName: String): File {
            try {
                // Создаем файл в каталоге filesDir с указанным именем
                val file = File(context.filesDir, fileName)

                // Создаем поток для записи в файл
                val stream = FileOutputStream(file)

                // Сжимаем изображение и записываем его в файл в формате PNG
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                // Закрываем поток
                stream.close()

                // Возвращаем созданный файл
                return file
            } catch (e: IOException) {
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
    }
}