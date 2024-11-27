package com.pavlov.nearWarSecrets.ui.theme

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.util.APK
import com.pavlov.nearWarSecrets.util.APKM

object ThemeManager {

    fun applyTheme(context: Context) { //достаем ночную тему из памяти и применяем в активности вызова
        val nightModeEnabled = APKM(context).getBoolean(APK.KEY_DARK_MODE, false)
        if (nightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun applyUserSwitch(context: Context): Int {
        val userSwitchEnabled = APKM(context).getBoolean(APK.KEY_USER_SWITCH, false)
        var background: Int = R.drawable.cat_background2

        if (context is Activity) {
            if (userSwitchEnabled) {
                background = R.drawable.mountains
            } else {
                background = R.drawable.cat_background2
            }
        }
        return background
    }

    fun isNightModeEnabled(context: Context): Boolean {
        return APKM(context).getBoolean(APK.KEY_DARK_MODE, false)
    }

    fun setNightModeEnabled(context: Context, isChecked: Boolean) {
        APKM(context).putBoolean(APK.KEY_DARK_MODE, isChecked)
    }

    fun saveUserSwitch(context: Context, isChecked: Boolean) {
        APKM(context).putBoolean(APK.KEY_USER_SWITCH, isChecked)
    }

    fun isUserSwitchEnabled(context: Context): Boolean {
        return APKM(context).getBoolean(APK.KEY_USER_SWITCH, false)
    }
}