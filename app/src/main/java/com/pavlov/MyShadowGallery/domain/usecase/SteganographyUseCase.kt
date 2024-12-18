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
}