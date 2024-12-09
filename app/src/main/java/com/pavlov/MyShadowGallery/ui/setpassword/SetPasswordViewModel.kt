package com.pavlov.MyShadowGallery.ui.setpassword

import androidx.lifecycle.ViewModel
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SetPasswordViewModel @Inject constructor(
    private val apkm: APKM
) : ViewModel() {

    fun savePassword(newPassword: String) {
        apkm.saveMastersSecret(newPassword, APK.KEY_SMALL_SECRET)
        apkm.putBoolean(APK.KEY_EXIST_OF_PASSWORD, true)
    }
}