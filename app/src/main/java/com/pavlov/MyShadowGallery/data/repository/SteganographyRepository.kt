package com.pavlov.MyShadowGallery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.BitSet
import javax.inject.Inject

class SteganographyRepository @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val TAG = "SteganographyRepository"

    suspend fun hideImageInMeme(
        memeBitmap: Bitmap,
        originalBitmap: Bitmap
    ): Flow<StegoEvent> = flow {
        emit(StegoEvent.Progress("Начало скрытия изображения в мем"))

        try {
            // Шаг 1: Конвертируем originalBitmap в байтовый массив
            emit(StegoEvent.Progress("Конвертация оригинального изображения в байтовый массив"))
            val byteArray = bitmapToByteArray(originalBitmap)

            // Шаг 2: Подготавливаем заголовок
            emit(StegoEvent.Progress("Подготовка заголовка"))
            val dataLength = byteArray.size * 8 // длина в битах
            val headerBytes = ByteArray(4)
            headerBytes[0] = (dataLength shr 24).toByte()
            headerBytes[1] = (dataLength shr 16).toByte()
            headerBytes[2] = (dataLength shr 8).toByte()
            headerBytes[3] = (dataLength).toByte()
            val totalData = headerBytes + byteArray

            // Шаг 3: Конвертируем totalData в BitSet
            emit(StegoEvent.Progress("Конвертация данных в биты"))
            val dataBitSet = byteArrayToBitSet(totalData)

            // Шаг 4: Встраиваем dataBits в memeBitmap
            emit(StegoEvent.Progress("Встраивание данных в мем изображение"))
            val encodedBitmap = memeBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val memeWidth = memeBitmap.width
            val memeHeight = memeBitmap.height
            val totalPixels = memeWidth * memeHeight

            // Проверяем, достаточно ли места для встраивания данных
            val availableBits = totalPixels * BITS_PER_PIXEL
            if (dataBitSet.length() > availableBits) {
                emit(StegoEvent.Error("Мем изображение слишком маленькое для скрытия данных"))
                return@flow
            }

            var bitIndex = 0
            for (y in 0 until memeHeight) {
                for (x in 0 until memeWidth) {
                    if (bitIndex >= dataBitSet.length()) {
                        break
                    }
                    val pixel = memeBitmap.getPixel(x, y)

                    // Получаем следующие BITS_PER_PIXEL бит
                    val bitsToEmbed = getNextBits(dataBitSet, bitIndex, BITS_PER_PIXEL)
                    bitIndex += BITS_PER_PIXEL

                    // Очистка последних 4 бит каждого цветового канала
                    val red = (pixel shr 16) and 0xF0
                    val green = (pixel shr 8) and 0xF0
                    val blue = pixel and 0xF0

                    // Встраивание новых бит
                    val newRed = red or ((bitsToEmbed shr 8) and 0x0F)
                    val newGreen = green or ((bitsToEmbed shr 4) and 0x0F)
                    val newBlue = blue or (bitsToEmbed and 0x0F)

                    val newPixel = (0xFF shl 24) or (newRed shl 16) or (newGreen shl 8) or newBlue

                    encodedBitmap.setPixel(x, y, newPixel)
                }
            }

            emit(StegoEvent.Progress("Скрытие изображения завершено"))
            emit(StegoEvent.Progress("Сохранение закодированного изображения..."))

            val fileName = "meme_with_hidden_image_${System.currentTimeMillis()}.png"
            val destinationFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(destinationFile)
            encodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            emit(StegoEvent.Progress("Закодированное изображение сохранено: ${destinationFile.absolutePath}"))

            emit(StegoEvent.Progress("Генерация URI"))
            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destinationFile
                )
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Ошибка при получении URI для файла: ${destinationFile.absolutePath}")
                emit(StegoEvent.Error("Ошибка при получении URI для файла: ${destinationFile.absolutePath}"))
                null
            }

            if (uri != null) {
                Timber.tag(TAG).d("Сгенерирован URI для закодированного изображения: $uri")
                emit(StegoEvent.Success(uri))
            } else {
                emit(StegoEvent.Error("Не удалось сгенерировать URI для файла: ${destinationFile.absolutePath}"))
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Ошибка в методе hideImageInMeme")
            emit(StegoEvent.Error("Ошибка при скрытии изображения: ${e.message}"))
        }
    }.flowOn(Dispatchers.Default)

    suspend fun extractOriginalFromMeme(
        memeUri: Uri
    ): Flow<StegoEvent> = flow {
        try {
            emit(StegoEvent.Progress("Открытие мем изображения из URI"))
            val inputStream = context.contentResolver.openInputStream(memeUri)
            val memeBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (memeBitmap == null) {
                emit(StegoEvent.Error("Не удалось декодировать мем из URI: $memeUri"))
                return@flow
            }

            // Шаг 1: Извлечение всех битов из изображения
            emit(StegoEvent.Progress("Извлечение всех битов из мем изображения"))
            val allBits = BitSet(memeBitmap.width * memeBitmap.height * BITS_PER_PIXEL)
            var bitIndex = 0

            for (y in 0 until memeBitmap.height) {
                for (x in 0 until memeBitmap.width) {
                    val pixel = memeBitmap.getPixel(x, y)

                    // Извлечение последних 4 бит каждого цветового канала
                    val red = (pixel shr 16) and 0x0F
                    val green = (pixel shr 8) and 0x0F
                    val blue = pixel and 0x0F

                    // Установка битов в BitSet
                    for (i in 3 downTo 0) {
                        allBits.set(bitIndex++, (red shr i) and 1 == 1)
                    }
                    for (i in 3 downTo 0) {
                        allBits.set(bitIndex++, (green shr i) and 1 == 1)
                    }
                    for (i in 3 downTo 0) {
                        allBits.set(bitIndex++, (blue shr i) and 1 == 1)
                    }
                }
            }

            // Шаг 2: Извлечение заголовка (первые 32 бита)
            emit(StegoEvent.Progress("Извлечение заголовка данных"))
            val headerBits = allBits.get(0, 32)
            val headerBytes = ByteArray(4)
            for (i in 0 until 4) { // 4 байта
                var byte = 0
                for (bit in 0 until 8) {
                    byte = (byte shl 1) or (if (headerBits.get(i * 8 + bit)) 1 else 0)
                }
                headerBytes[i] = byte.toByte()
            }

            // Парсинг длины данных
            val dataLength = (
                    ((headerBytes[0].toInt() and 0xFF) shl 24) or
                            ((headerBytes[1].toInt() and 0xFF) shl 16) or
                            ((headerBytes[2].toInt() and 0xFF) shl 8) or
                            (headerBytes[3].toInt() and 0xFF)
                    )
            emit(StegoEvent.Progress("Длина данных: $dataLength бит"))

            // Шаг 3: Извлечение данных
            emit(StegoEvent.Progress("Извлечение данных из мем изображения"))
            val dataBitsToExtract = dataLength
            val dataBitSet = BitSet(dataBitsToExtract)
            for (i in 0 until dataBitsToExtract) {
                if (32 + i >= allBits.length()) {
                    emit(StegoEvent.Error("Недостаточно бит для данных"))
                    return@flow
                }
                dataBitSet.set(i, allBits.get(32 + i))
            }

            // Шаг 4: Конвертация битов в байты
            emit(StegoEvent.Progress("Конвертация битов в байты"))
            val dataBytes = ByteArray((dataLength + 7) / 8) // Округление вверх
            for (i in dataBytes.indices) {
                var byte = 0
                for (bit in 0 until 8) {
                    val bitPosition = i * 8 + bit
                    if (bitPosition < dataLength) {
                        byte = (byte shl 1) or (if (dataBitSet.get(bitPosition)) 1 else 0)
                    } else {
                        byte = (byte shl 1)
                    }
                }
                dataBytes[i] = byte.toByte()
            }

            // Шаг 5: Конвертация байтов в Bitmap
            emit(StegoEvent.Progress("Преобразование байтов в изображение"))
            val originalBitmap = byteArrayToBitmap(dataBytes)
            if (originalBitmap == null) {
                emit(StegoEvent.Error("Не удалось преобразовать байты в Bitmap"))
                return@flow
            }

            emit(StegoEvent.Progress("Извлечение изображения завершено"))
            val extractedUri = writeBitmapToFile(originalBitmap)
            if (extractedUri != null) {
                emit(StegoEvent.Success(extractedUri)) // Эмиссия Success события
            } else {
                emit(StegoEvent.Error("Не удалось сохранить извлеченное изображение"))
            }
            emit(StegoEvent.Progress("Генерация URI завершена"))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Ошибка при извлечении оригинального изображения из мема: $memeUri")
            emit(StegoEvent.Error("Ошибка при извлечении изображения: ${e.message}"))
        }
    }.flowOn(Dispatchers.Default)

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun byteArrayToBitSet(byteArray: ByteArray): BitSet {
        val bitSet = BitSet(byteArray.size * 8)
        for (i in byteArray.indices) {
            for (bit in 0 until 8) {
                val bitValue = (byteArray[i].toInt() shr (7 - bit)) and 1
                bitSet.set(i * 8 + bit, bitValue == 1)
            }
        }
        return bitSet
    }

    private fun getNextBits(bitSet: BitSet, start: Int, count: Int): Int {
        var value = 0
        for (i in 0 until count) {
            value = (value shl 1) or (if (bitSet.get(start + i)) 1 else 0)
        }
        return value
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun writeBitmapToFile(bitmap: Bitmap): Uri? {
        return try {
            val fileName = "extracted_image_${System.currentTimeMillis()}.png"
            val destinationFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(destinationFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destinationFile
            )
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при сохранении извлеченного изображения")
            null
        }
    }

    companion object {
        const val HEADER_SIZE = 4 // Количество байт для заголовка (длина данных)
        const val BITS_PER_PIXEL = 12 // Количество бит для встраивания в один пиксель (4 бита на канал)
    }
}
