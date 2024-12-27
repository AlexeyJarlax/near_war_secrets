package com.pavlov.MyShadowGallery.data.repository

import com.pavlov.MyShadowGallery.R
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
        emit(StegoEvent.Progress(context.getString(R.string.stego_start_hiding)))

        try {
            // Шаг 1: Конвертируем originalBitmap в байтовый массив с возможной компрессией
            emit(StegoEvent.Progress(context.getString(R.string.stego_convert_original)))

            // Определяем доступные биты для скрытого изображения
            val memeWidth = memeBitmap.width
            val memeHeight = memeBitmap.height
            val availableBits = memeWidth * memeHeight * BITS_PER_PIXEL - HEADER_SIZE * 8 // Вычитаем биты заголовка
            val maxDataLength = availableBits / 8 // Максимальный размер в байтах

            Timber.tag(TAG).d("Available bits: $availableBits, Max data length: $maxDataLength bytes")

            // Начальные параметры компрессии
            var currentBitmap = originalBitmap
            var compressionQuality = 100
            var byteArray = bitmapToByteArray(currentBitmap, compressionQuality)

            // Итеративно уменьшаем качество или масштабируем изображение до тех пор, пока размер не станет допустимым
            while (byteArray.size > maxDataLength) {
                compressionQuality -= 5
                if (compressionQuality < 10) {
                    // Если качество слишком низкое, начинаем масштабирование
                    val scaleFactor = 0.9f
                    val newWidth = (currentBitmap.width * scaleFactor).toInt()
                    val newHeight = (currentBitmap.height * scaleFactor).toInt()
                    if (newWidth < 1 || newHeight < 1) {
                        emit(StegoEvent.Error(context.getString(R.string.stego_meme_too_small)))
                        return@flow
                    }
                    Timber.tag(TAG).d("Scaling hidden image to ${newWidth}x${newHeight}")
                    currentBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
                    compressionQuality = 100 // Сброс качества после масштабирования
                    emit(StegoEvent.Progress(context.getString(R.string.stego_compression_completed)))
                } else {
                    Timber.tag(TAG).d("Reducing compression quality to $compressionQuality")
                }
                byteArray = bitmapToByteArray(currentBitmap, compressionQuality)
                Timber.tag(TAG).d("Current hidden data size: ${byteArray.size} bytes")
            }

            Timber.tag(TAG).d("Final hidden data size: ${byteArray.size} bytes")

            // Шаг 2: Подготавливаем заголовок
            emit(StegoEvent.Progress(context.getString(R.string.stego_prepare_header)))
            val dataLength = byteArray.size * 8 // длина в битах
            val headerBytes = ByteArray(HEADER_SIZE)
            headerBytes[0] = (dataLength shr 24).toByte()
            headerBytes[1] = (dataLength shr 16).toByte()
            headerBytes[2] = (dataLength shr 8).toByte()
            headerBytes[3] = (dataLength).toByte()
            val totalData = headerBytes + byteArray

            Timber.tag(TAG).d("Total data length (bits): $dataLength")

            // Шаг 3: Конвертируем totalData в BitSet
            emit(StegoEvent.Progress(context.getString(R.string.stego_convert_bits)))
            val dataBitSet = byteArrayToBitSet(totalData)

            // Шаг 4: Встраиваем dataBits в memeBitmap
            emit(StegoEvent.Progress(context.getString(R.string.stego_embed_data)))
            val encodedBitmap = memeBitmap.copy(Bitmap.Config.ARGB_8888, true)

            var bitIndex = 0
            outer@ for (y in 0 until memeHeight) {
                for (x in 0 until memeWidth) {
                    if (bitIndex >= dataBitSet.length()) {
                        break@outer
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

            emit(StegoEvent.Progress(context.getString(R.string.stego_hiding_completed)))
            emit(StegoEvent.Progress(context.getString(R.string.stego_saving_image)))

            val fileName = "meme_${System.currentTimeMillis()}.png" // Изменено на PNG
            val destinationFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(destinationFile)
            encodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // Изменено на PNG
            outputStream.flush()
            outputStream.close()

            emit(StegoEvent.Progress(context.getString(R.string.stego_image_saved, destinationFile.absolutePath)))

            emit(StegoEvent.Progress(context.getString(R.string.stego_generating_uri)))
            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destinationFile
                )
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, context.getString(R.string.stego_uri_error, destinationFile.absolutePath))
                emit(StegoEvent.Error(context.getString(R.string.stego_uri_error, destinationFile.absolutePath)))
                null
            }

            if (uri != null) {
                Timber.tag(TAG).d("Сгенерирован URI для закодированного изображения: $uri")
                emit(StegoEvent.Success(uri))
            } else {
                emit(StegoEvent.Error(context.getString(R.string.stego_uri_failed, destinationFile.absolutePath)))
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Ошибка в методе hideImageInMeme")
            emit(StegoEvent.Error(context.getString(R.string.stego_hide_error, e.message ?: "Неизвестная ошибка")))
        }
    }.flowOn(Dispatchers.Default)

    private fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 100): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)
        return outputStream.toByteArray()
    }

    suspend fun extractOriginalFromMeme(
        memeUri: Uri
    ): Flow<StegoEvent> = flow {
        try {
            emit(StegoEvent.Progress(context.getString(R.string.stego_opening_meme)))
            val inputStream = context.contentResolver.openInputStream(memeUri)
            val memeBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (memeBitmap == null) {
                emit(StegoEvent.Error(context.getString(R.string.stego_decode_error, memeUri)))
                return@flow
            }

            // Шаг 1: Извлечение всех битов из изображения
            emit(StegoEvent.Progress(context.getString(R.string.stego_extracting_bits)))
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
            emit(StegoEvent.Progress(context.getString(R.string.stego_extract_header)))
            val headerBits = allBits.get(0, HEADER_SIZE * 8)
            val headerBytes = ByteArray(HEADER_SIZE)
            for (i in 0 until HEADER_SIZE) { // 4 байта
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
            val dataLengthInBits = dataLength
            val dataLengthInMegabits = dataLength / 1_000_000.0 // Перевод в мегабиты
            emit(StegoEvent.Progress(context.getString(R.string.stego_data_length, dataLengthInBits, dataLengthInMegabits)))

            // Шаг 3: Извлечение данных
            emit(StegoEvent.Progress(context.getString(R.string.stego_extract_data)))
            val dataBitsToExtract = dataLength
            val dataBitSet = BitSet(dataBitsToExtract)
            for (i in 0 until dataBitsToExtract) {
                if (HEADER_SIZE * 8 + i >= allBits.length()) {
                    emit(StegoEvent.Error(context.getString(R.string.stego_extract_error, "Недостаточно бит для данных")))
                    return@flow
                }
                dataBitSet.set(i, allBits.get(HEADER_SIZE * 8 + i))
            }

            // Шаг 4: Конвертация битов в байты
            emit(StegoEvent.Progress(context.getString(R.string.stego_convert_bytes)))
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

            Timber.tag(TAG).d("Extracted data bytes size: ${dataBytes.size}")

            // Шаг 5: Конвертация байтов в Bitmap
            emit(StegoEvent.Progress(context.getString(R.string.stego_convert_to_bitmap)))
            val originalBitmap = byteArrayToBitmap(dataBytes)
            if (originalBitmap == null) {
                emit(StegoEvent.Error(context.getString(R.string.stego_bitmap_conversion_failed)))
                return@flow
            }

            emit(StegoEvent.Progress(context.getString(R.string.stego_extraction_completed)))
            val extractedUri = writeBitmapToFile(originalBitmap)
            if (extractedUri != null) {
                emit(StegoEvent.Success(extractedUri)) // Эмиссия Success события
            } else {
                emit(StegoEvent.Error(context.getString(R.string.stego_save_extracted_failed)))
            }
            emit(StegoEvent.Progress(context.getString(R.string.stego_generate_uri_completed)))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Ошибка при извлечении оригинального изображения из мема: $memeUri")
            emit(StegoEvent.Error(context.getString(R.string.stego_extract_error, e.message ?: "Неизвестная ошибка")))
        }
    }.flowOn(Dispatchers.Default)


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
