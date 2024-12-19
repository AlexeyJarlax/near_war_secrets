package com.pavlov.MyShadowGallery.data.repository

/** методы шифрования и дешифрования */

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
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import java.util.BitSet

class SteganographyRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TAG = "SteganographyRepository"

    private val secretKey: SecretKey = generateSecretKey()

    private fun generateSecretKey(): SecretKey {
        // Важно: В реальном приложении используйте безопасные методы хранения ключей
        val keyBytes = "ThisIsASecretKeyForAES256Encryption!".toByteArray(Charsets.UTF_8)
        return SecretKeySpec(keyBytes.copyOf(32), "AES") // 32 байта (256 бит)
    }

    private fun encrypt(data: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivBytes = ByteArray(16)
            SecureRandom().nextBytes(ivBytes)
            val ivSpec = IvParameterSpec(ivBytes)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encrypted = cipher.doFinal(data)
            // Предваряем зашифрованные данные IV для последующей дешифровки
            ivBytes + encrypted
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при шифровании данных")
            null
        }
    }

    private fun decrypt(data: ByteArray): ByteArray? {
        return try {
            if (data.size < 16) {
                Timber.e("Данные слишком короткие для IV")
                return null
            }
            val ivBytes = data.copyOfRange(0, 16)
            val encryptedBytes = data.copyOfRange(16, data.size)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            cipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при дешифровании данных")
            null
        }
    }

    suspend fun hideImageInMeme(
        memeBitmap: Bitmap,
        originalBitmap: Bitmap
    ): Flow<StegoEvent> = flow {
        emit(StegoEvent.Progress("Начало скрытия изображения в мем"))

        try {
            // Шаг 1: Конвертируем originalBitmap в байтовый массив
            emit(StegoEvent.Progress("Конвертация оригинального изображения в байтовый массив"))
            val byteArray = bitmapToByteArray(originalBitmap)

            // Шаг 2: Шифруем байтовый массив с использованием AES-256
            emit(StegoEvent.Progress("Шифрование данных изображения"))
            val encryptedBytes = encrypt(byteArray)
            if (encryptedBytes == null) {
                emit(StegoEvent.Error("Не удалось зашифровать данные изображения"))
                return@flow
            }

            // Шаг 3: Подготавливаем заголовок
            emit(StegoEvent.Progress("Подготовка заголовка"))
            val dataLength = encryptedBytes.size * 8 // длина в битах
            val headerBytes = ByteArray(4)
            headerBytes[0] = (dataLength shr 24).toByte()
            headerBytes[1] = (dataLength shr 16).toByte()
            headerBytes[2] = (dataLength shr 8).toByte()
            headerBytes[3] = (dataLength).toByte()
            val totalData = headerBytes + encryptedBytes

            // Шаг 4: Конвертируем totalData в BitSet
            emit(StegoEvent.Progress("Конвертация данных в биты"))
            val dataBitSet = byteArrayToBitSet(totalData)

            // Шаг 5: Встраиваем dataBits в memeBitmap
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
    }.flowOn(Dispatchers.Default) // Установка контекста эмиссии на Dispatchers.Default

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

            // Шаг 1: Извлечение заголовка (4 байта)
            emit(StegoEvent.Progress("Извлечение заголовка данных"))
            val headerBits = BitSet(32)
            var bitIndex = 0

            for (y in 0 until memeBitmap.height) {
                for (x in 0 until memeBitmap.width) {
                    if (bitIndex >= 32) { // 32 бит для заголовка
                        break
                    }
                    val pixel = memeBitmap.getPixel(x, y)

                    // Извлечение последних 4 бит каждого цветового канала
                    val red = (pixel shr 16) and 0x0F
                    val green = (pixel shr 8) and 0x0F
                    val blue = pixel and 0x0F

                    // Конвертация в биты
                    for (i in 3 downTo 0) { // 4 бита на канал
                        headerBits.set(bitIndex++, (red shr i) and 1 == 1)
                    }
                    for (i in 3 downTo 0) {
                        headerBits.set(bitIndex++, (green shr i) and 1 == 1)
                    }
                    for (i in 3 downTo 0) {
                        headerBits.set(bitIndex++, (blue shr i) and 1 == 1)
                    }

                    if (bitIndex >= 32) break
                }
            }

            if (headerBits.length() < 32) {
                emit(StegoEvent.Error("Малое количество бит в заголовке"))
                return@flow
            }

            // Конвертация заголовочных бит в байты
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

            // Шаг 2: Извлечение данных
            emit(StegoEvent.Progress("Извлечение данных из мем изображения"))
            val dataBitsToExtract = dataLength
            val dataBitSet = BitSet(dataBitsToExtract)
            bitIndex = 32 // После заголовка

            for (y in 0 until memeBitmap.height) {
                for (x in 0 until memeBitmap.width) {
                    // Пропускаем пиксели заголовка
                    val pixelIndex = y * memeBitmap.width + x
                    val bitsPerPixel = BITS_PER_PIXEL
                    if (pixelIndex * bitsPerPixel < bitIndex) {
                        continue
                    }

                    if (dataBitSet.length() >= dataBitsToExtract) {
                        break
                    }

                    val pixel = memeBitmap.getPixel(x, y)

                    // Извлечение последних 4 бит каждого цветового канала
                    val red = (pixel shr 16) and 0x0F
                    val green = (pixel shr 8) and 0x0F
                    val blue = pixel and 0x0F

                    // Конвертация в биты
                    for (i in 3 downTo 0) {
                        if (dataBitSet.length() < dataBitsToExtract) {
                            dataBitSet.set(dataBitSet.length(), (red shr i) and 1 == 1)
                        }
                    }
                    for (i in 3 downTo 0) {
                        if (dataBitSet.length() < dataBitsToExtract) {
                            dataBitSet.set(dataBitSet.length(), (green shr i) and 1 == 1)
                        }
                    }
                    for (i in 3 downTo 0) {
                        if (dataBitSet.length() < dataBitsToExtract) {
                            dataBitSet.set(dataBitSet.length(), (blue shr i) and 1 == 1)
                        }
                    }
                }
            }

            if (dataBitSet.length() < dataBitsToExtract) {
                emit(StegoEvent.Error("Недостаточно бит для данных"))
                return@flow
            }

            // Конвертация битов в байты
            emit(StegoEvent.Progress("Конвертация битов в байты"))
            val dataBytes = ByteArray((dataLength + 7) / 8) // Округление вверх
            for (i in dataBytes.indices) {
                var byte = 0
                for (bit in 0 until 8) {
                    val bitPosition = i * 8 + bit
                    if (bitPosition < dataBitsToExtract) {
                        byte = (byte shl 1) or (if (dataBitSet.get(bitPosition)) 1 else 0)
                    } else {
                        byte = (byte shl 1)
                    }
                }
                dataBytes[i] = byte.toByte()
            }

            // Шаг 3: Дешифрование данных
            emit(StegoEvent.Progress("Дешифрование данных"))
            val decryptedBytes = decrypt(dataBytes)
            if (decryptedBytes == null) {
                emit(StegoEvent.Error("Не удалось дешифровать данные"))
                return@flow
            }

            // Шаг 4: Конвертация дешифрованных байтов в Bitmap
            emit(StegoEvent.Progress("Преобразование байтов в изображение"))
            val originalBitmap = byteArrayToBitmap(decryptedBytes)

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
