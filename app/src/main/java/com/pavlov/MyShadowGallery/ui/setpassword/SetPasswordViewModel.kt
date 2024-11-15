package com.pavlov.MyShadowGallery.ui.setpassword

import androidx.lifecycle.ViewModel
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM

class SetPasswordViewModel(
    private val apkm: APKM
) : ViewModel() {

    val isPasswordExist = apkm.getMastersSecret(APK.KEY_SMALL_SECRET).isNotBlank()

    fun savePassword(newPassword: String) {
        apkm.saveMastersSecret(newPassword, APK.KEY_SMALL_SECRET)
        apkm.saveBooleanToSPK(APK.KEY_EXIST_OF_PASSWORD, true)
    }
}