package com.practicum.kototeka.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.kototeka.R

object ThemeManager {


    fun applyTheme(context: Context) { //достаем ночную тему из памяти и применяем в активности вызова
        val sharedPreferences =
            context.getSharedPreferences(MyCompObj.PREFS_NAME, Context.MODE_PRIVATE)
        val nightModeEnabled = sharedPreferences.getBoolean(MyCompObj.KEY_NIGHT_MODE, false)
        if (nightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun applyUserSwitch(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(MyCompObj.PREFS_NAME, Context.MODE_PRIVATE)
        val userSwitchEnabled = sharedPreferences.getBoolean(MyCompObj.USER_SWITCH, false)

        // Задаем значение по умолчанию
        var background: Int = R.drawable.cat

        if (context is Activity) {
            if (userSwitchEnabled) {
                background = R.drawable.mountains
            } else {
                background = R.drawable.cat
            }
        }
        return background
    }

    fun isNightModeEnabled(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(MyCompObj.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(MyCompObj.KEY_NIGHT_MODE, false)
    }

    fun setNightModeEnabled(context: Context, enabled: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(MyCompObj.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(MyCompObj.KEY_NIGHT_MODE, enabled)
        editor.apply()
        applyTheme(context)
    }

    fun saveUserSwitch(context: Context, isChecked: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(MyCompObj.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(MyCompObj.USER_SWITCH, isChecked)
        editor.apply()
        applyUserSwitch(context)
    }

    fun isUserSwitchEnabled(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(MyCompObj.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(MyCompObj.USER_SWITCH, false)
    }
}