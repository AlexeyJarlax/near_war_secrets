package com.pavlov.MyShadowGallery.data.repository

/** для проброса результатов операций шифрования в UI, класс будет представлять типы событий: прогресс ошибки успех. */

import android.net.Uri

sealed class StegoEvent {
    data class Progress(val message: String) : StegoEvent()
    data class Error(val message: String) : StegoEvent()
    data class Success(val uri: Uri) : StegoEvent()
}