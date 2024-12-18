package com.pavlov.MyShadowGallery.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.pavlov.MyShadowGallery.data.repository.SteganographyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SteganographyUseCase @Inject constructor(
    private val steganographyRepository: SteganographyRepository
) {

    suspend fun hideImage(memeBitmap: Bitmap, originalBitmap: Bitmap): Bitmap? {
        return steganographyRepository.hideImageInMeme(memeBitmap, originalBitmap)
    }

    suspend fun extractOriginalImage(memeUri: Uri): Uri? {
        return steganographyRepository.extractOriginalFromMeme(memeUri)
    }

    fun hasMarker(bitmap: Bitmap): Boolean {
        var extractedCode = 0
        for (i in 0 until SteganographyRepository.SteganographyConstants.HEADER_SIZE) {
            if (i >= bitmap.width * bitmap.height) {
                return false
            }
            val x = i % bitmap.width
            val y = i / bitmap.width
            val pixel = bitmap.getPixel(x, y)
            val headerBits = pixel and 0xF
            extractedCode = (extractedCode shl 4) or headerBits
        }
        return extractedCode == SteganographyRepository.SteganographyConstants.HEADER_CODE
    }
}