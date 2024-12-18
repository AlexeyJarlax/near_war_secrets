package com.pavlov.MyShadowGallery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SteganographyRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TAG = "SteganographyRepository"

    suspend fun hideImageInMeme(memeBitmap: Bitmap, originalBitmap: Bitmap): Bitmap? {
        Timber.d("Начало скрытия изображения в мем")
        return withContext(Dispatchers.Default) {
            try {
                val memeWidth = memeBitmap.width
                val memeHeight = memeBitmap.height
                val originalWidth = originalBitmap.width
                val originalHeight = originalBitmap.height

                val encodedBitmap = memeBitmap.copy(Bitmap.Config.ARGB_8888, true)

                // Вставка маркера в первые HEADER_SIZE пикселей
                for (i in 0 until SteganographyConstants.HEADER_SIZE) {
                    val shift = (SteganographyConstants.HEADER_SIZE - 1 - i) * 4
                    val headerBits = (SteganographyConstants.HEADER_CODE shr shift) and 0xF
                    if (i < memeWidth * memeHeight) {
                        val x = i % memeWidth
                        val y = i / memeWidth
                        val originalPixel = memeBitmap.getPixel(x, y)
                        val newPixel = (originalPixel and 0xFFFFFFF0.toInt()) or headerBits
                        encodedBitmap.setPixel(x, y, newPixel)
                    }
                }

                for (y in 0 until memeHeight) {
                    for (x in 0 until memeWidth) {
                        val pixelIndex = y * memeWidth + x
                        if (pixelIndex < SteganographyConstants.HEADER_SIZE) {
                            continue
                        }

                        val memePixel = memeBitmap.getPixel(x, y)

                        if (x < originalWidth && y < originalHeight) {
                            val originalPixel = originalBitmap.getPixel(x, y)
                            val encodedPixel = encodePixel(memePixel, originalPixel)
                            encodedBitmap.setPixel(x, y, encodedPixel)
                        }
                    }
                }
                Timber.d("Скрытие изображения завершено")
                encodedBitmap
            } catch (e: Exception) {
                Timber.e(e, "Ошибка в методе hideImageInMeme")
                null
            }
        }
    }

    suspend fun extractOriginalFromMeme(memeUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(memeUri)
                val memeBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (memeBitmap == null) {
                    Timber.e("Не удалось декодировать мем из URI: $memeUri")
                    return@withContext null
                }

                val originalBitmap = extractOriginal(memeBitmap)

                if (originalBitmap == null) {
                    Timber.e("Не удалось извлечь оригинальное изображение из мема.")
                    return@withContext null
                }

                val fileName = "original_image_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(destinationFile)
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destinationFile
                )

                uri
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при извлечении оригинального изображения из мема: $memeUri")
                null
            }
        }
    }

    private fun extractOriginal(memeBitmap: Bitmap): Bitmap? {
        val width = memeBitmap.width
        val height = memeBitmap.height

        val originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val memePixel = memeBitmap.getPixel(x, y)

                val encodedRed = (memePixel shr 16) and 0x0F
                val encodedGreen = (memePixel shr 8) and 0x0F
                val encodedBlue = memePixel and 0x0F

                val originalRed = encodedRed shl 4
                val originalGreen = encodedGreen shl 4
                val originalBlue = encodedBlue shl 4

                val originalPixel =
                    (0xFF shl 24) or (originalRed shl 16) or (originalGreen shl 8) or originalBlue

                originalBitmap.setPixel(x, y, originalPixel)
            }
        }

        return originalBitmap
    }

    private fun encodePixel(memePixel: Int, originalPixel: Int): Int {
        val memeRed = (memePixel shr 16) and 0xFF
        val memeGreen = (memePixel shr 8) and 0xFF
        val memeBlue = memePixel and 0xFF

        val originalRed = (originalPixel shr 16) and 0xFF
        val originalGreen = (originalPixel shr 8) and 0xFF
        val originalBlue = originalPixel and 0xFF

        val encodedRed = (memeRed and 0xF0) or (originalRed shr 4)
        val encodedGreen = (memeGreen and 0xF0) or (originalGreen shr 4)
        val encodedBlue = (memeBlue and 0xF0) or (originalBlue shr 4)

        return (0xFF shl 24) or (encodedRed shl 16) or (encodedGreen shl 8) or encodedBlue
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 1001
    }

    object SteganographyConstants {
        const val HEADER_CODE: Int = 0xFACEB00C.toInt() // Уникальный код маркера
        const val HEADER_SIZE = 8 // Количество пикселей, используемых для маркера
    }
}
