package com.pavlov.MyShadowGallery.domain.usecase

/**  проброс функций шифрования дешифрования */

import android.graphics.Bitmap
import android.net.Uri
import com.pavlov.MyShadowGallery.data.repository.SteganographyRepository
import com.pavlov.MyShadowGallery.data.repository.StegoEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SteganographyUseCase @Inject constructor(
    private val steganographyRepository: SteganographyRepository
) {

    suspend fun hideImage(
        memeBitmap: Bitmap,
        originalBitmap: Bitmap
    ): Flow<StegoEvent> {
        return steganographyRepository.hideImageInMeme(memeBitmap, originalBitmap)
    }

    suspend fun extractOriginalImage(
        memeUri: Uri
    ): Flow<StegoEvent> {
        return steganographyRepository.extractOriginalFromMeme(memeUri)
    }
}