package com.pavlov.MyShadowGallery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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

    suspend fun hideImageInMeme(memeBitmap: Bitmap, originalBitmap: Bitmap): Bitmap? {
        Timber.d("Начало скрытия изображения в мем")
        return withContext(Dispatchers.Default) {
            try {
                // Шаг 1: Конвертируем originalBitmap в байтовый массив
                val byteArray = bitmapToByteArray(originalBitmap)

                // Шаг 2: Шифруем байтовый массив с использованием AES-256
                val encryptedBytes = encrypt(byteArray)
                if (encryptedBytes == null) {
                    Timber.e("Не удалось зашифровать данные изображения")
                    return@withContext null
                }

                // Шаг 3: Подготавливаем заголовок
                // Заголовок состоит только из длины зашифрованных данных в битах
                val dataLength = encryptedBytes.size * 8 // длина в битах, Int

                // Конвертируем длину в байтовый массив (4 байта для Int)
                val headerBytes = ByteArray(4)
                headerBytes[0] = (dataLength shr 24).toByte()
                headerBytes[1] = (dataLength shr 16).toByte()
                headerBytes[2] = (dataLength shr 8).toByte()
                headerBytes[3] = (dataLength).toByte()

                // Объединяем заголовок и зашифрованные данные
                val totalData = headerBytes + encryptedBytes

                // Шаг 4: Конвертируем totalData в массив бит
                val dataBits = byteArrayToBitArray(totalData)

                // Шаг 5: Встраиваем dataBits в memeBitmap
                val encodedBitmap = memeBitmap.copy(Bitmap.Config.ARGB_8888, true)
                val memeWidth = memeBitmap.width
                val memeHeight = memeBitmap.height
                val totalPixels = memeWidth * memeHeight

                // Проверяем, достаточно ли места для встраивания данных
                val availableBits = totalPixels * BITS_PER_PIXEL
                if (dataBits.size > availableBits) {
                    Timber.e("Мем изображение слишком маленькое для скрытия данных")
                    return@withContext null
                }

                var bitIndex = 0
                for (y in 0 until memeHeight) {
                    for (x in 0 until memeWidth) {
                        if (bitIndex >= dataBits.size) {
                            break
                        }
                        val pixel = memeBitmap.getPixel(x, y)

                        // Получаем следующие BITS_PER_PIXEL бит
                        val bitsToEmbed = getNextBits(dataBits, bitIndex, BITS_PER_PIXEL)
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
                val memeBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (memeBitmap == null) {
                    Timber.e("Не удалось декодировать мем из URI: $memeUri")
                    return@withContext null
                }

                // Шаг 1: Извлечение заголовка (4 байта)
                val headerBits = mutableListOf<Boolean>()
                val headerBytes = ByteArray(4) // 4 байта для длины данных
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
                            headerBits.add(((red shr i) and 1) == 1)
                        }
                        for (i in 3 downTo 0) {
                            headerBits.add(((green shr i) and 1) == 1)
                        }
                        for (i in 3 downTo 0) {
                            headerBits.add(((blue shr i) and 1) == 1)
                        }

                        bitIndex += 12
                    }
                }

                if (headerBits.size < 32) {
                    Timber.e("Малое количество бит в заголовке")
                    return@withContext null
                }

                // Конвертация заголовочных бит в байты
                for (i in 0 until 4) { // 4 байта
                    var byte = 0
                    for (bit in 0 until 8) {
                        byte = (byte shl 1) or (if (headerBits[i * 8 + bit]) 1 else 0)
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

                Timber.d("Длина данных: $dataLength бит")

                // Шаг 2: Извлечение данных
                val dataBitsToExtract = dataLength
                val dataBits = mutableListOf<Boolean>()
                bitIndex = 32 // После заголовка

                for (y in 0 until memeBitmap.height) {
                    for (x in 0 until memeBitmap.width) {
                        // Пропускаем пиксели заголовка
                        val pixelIndex = y * memeBitmap.width + x
                        val bitsPerPixel = BITS_PER_PIXEL
                        if (pixelIndex * bitsPerPixel < bitIndex) {
                            continue
                        }

                        if (dataBits.size >= dataBitsToExtract) {
                            break
                        }

                        val pixel = memeBitmap.getPixel(x, y)

                        // Извлечение последних 4 бит каждого цветового канала
                        val red = (pixel shr 16) and 0x0F
                        val green = (pixel shr 8) and 0x0F
                        val blue = pixel and 0x0F

                        // Конвертация в биты
                        for (i in 3 downTo 0) {
                            dataBits.add(((red shr i) and 1) == 1)
                        }
                        for (i in 3 downTo 0) {
                            dataBits.add(((green shr i) and 1) == 1)
                        }
                        for (i in 3 downTo 0) {
                            dataBits.add(((blue shr i) and 1) == 1)
                        }

                        bitIndex += 12
                    }
                }

                if (dataBits.size < dataBitsToExtract) {
                    Timber.e("Недостаточно бит для данных")
                    return@withContext null
                }

                // Конвертация битов в байты
                val dataBytes = ByteArray((dataLength + 7) / 8) // Округление вверх
                for (i in dataBytes.indices) {
                    var byte = 0
                    for (bit in 0 until 8) {
                        val bitPosition = i * 8 + bit
                        if (bitPosition < dataBits.size) {
                            byte = (byte shl 1) or (if (dataBits[bitPosition]) 1 else 0)
                        } else {
                            byte = (byte shl 1)
                        }
                    }
                    dataBytes[i] = byte.toByte()
                }

                // Шаг 3: Дешифрование данных
                val decryptedBytes = decrypt(dataBytes)
                if (decryptedBytes == null) {
                    Timber.e("Не удалось дешифровать данные")
                    return@withContext null
                }

                // Шаг 4: Конвертация дешифрованных байтов в Bitmap
                val originalBitmap = byteArrayToBitmap(decryptedBytes)

                if (originalBitmap == null) {
                    Timber.e("Не удалось преобразовать байты в Bitmap")
                    return@withContext null
                }

                Timber.d("Извлечение изображения завершено")
                return@withContext writeBitmapToFile(originalBitmap)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при извлечении оригинального изображения из мема: $memeUri")
                null
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun byteArrayToBitArray(byteArray: ByteArray): List<Boolean> {
        val bits = mutableListOf<Boolean>()
        for (byte in byteArray) {
            for (i in 7 downTo 0) {
                bits.add(((byte.toInt() shr i) and 1) == 1)
            }
        }
        return bits
    }

    private fun getNextBits(bits: List<Boolean>, start: Int, count: Int): Int {
        var value = 0
        for (i in 0 until count) {
            value = (value shl 1) or (if (bits[start + i]) 1 else 0)
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
