package com.pavlov.MyShadowGallery.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.pavlov.MyShadowGallery.R

object ThemeManager {

    fun applyTheme(context: Context) { //достаем ночную тему из памяти и применяем в активности вызова
        val nightModeEnabled = AppPreferencesKeysMethods(context).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_NIGHT_MODE)
        if (nightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun applyUserSwitch(context: Context): Int {
        val userSwitchEnabled = AppPreferencesKeysMethods(context).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_USER_SWITCH)
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
        return AppPreferencesKeysMethods(context).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_NIGHT_MODE)
    }

    fun setNightModeEnabled(context: Context, isChecked: Boolean) {
        AppPreferencesKeysMethods(context).saveBooleanToSharedPreferences(AppPreferencesKeys.KEY_NIGHT_MODE, isChecked)
    }

    fun saveUserSwitch(context: Context, isChecked: Boolean) {
        AppPreferencesKeysMethods(context).saveBooleanToSharedPreferences(AppPreferencesKeys.KEY_USER_SWITCH, isChecked)
    }

    fun isUserSwitchEnabled(context: Context): Boolean {
        return AppPreferencesKeysMethods(context).getBooleanFromSharedPreferences(AppPreferencesKeys.KEY_USER_SWITCH)
    }
}