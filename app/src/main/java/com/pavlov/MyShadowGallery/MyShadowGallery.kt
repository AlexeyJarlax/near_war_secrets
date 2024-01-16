package com.pavlov.MyShadowGallery;

import android.app.Activity
import android.app.Application
import android.content.Context
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import android.os.Bundle
import androidx.core.content.edit
import com.pavlov.MyShadowGallery.util.AppPreferencesKeysMethods

class MyShadowGallery : Application() {
    private var cleanupDone = false

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Ничего не делаем
            }

            override fun onActivityStarted(activity: Activity) {
                // Ничего не делаем
            }

            override fun onActivityResumed(activity: Activity) {
                // Ничего не делаем
            }

            override fun onActivityPaused(activity: Activity) {
                // Ничего не делаем
            }

            override fun onActivityStopped(activity: Activity) {
                // Ничего не делаем
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Ничего не делаем
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (!cleanupDone) {
                    cleanupOnPeekaboo()
                    cleanupDone = true
                }
            }
        })
    }

    override fun onTerminate() {
        cleanupOnPeekaboo()
        super.onTerminate()
    }

    private fun cleanupOnPeekaboo() {
        // Очистка файлов с расширением .peekaboo, .unknown и .k
        val folder = applicationContext.filesDir
        val peekabooFiles = folder.listFiles { _, name ->
            name.endsWith(".peekaboo") || name.endsWith(".unknown") || name.endsWith(".k") || name.endsWith(".dat")
        }

        peekabooFiles?.forEach { file ->
            file.delete()
        }

        // Проверка и удаление ключа шифрования, если флаг установлен
        val sharedPreferences =
            getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val shouldDeleteEk = sharedPreferences.getBoolean(
            AppPreferencesKeys.KEY_DELETE_EK_WHEN_CLOSING_THE_SESSION,
            false
        )

        if (shouldDeleteEk) {
            sharedPreferences.edit {
                AppPreferencesKeysMethods(context = applicationContext).delMastersSecret(
                    AppPreferencesKeys.KEY_BIG_SECRET
                )
                AppPreferencesKeysMethods(context = applicationContext).saveBooleanToSharedPreferences(
                    AppPreferencesKeys.KEY_EXIST_OF_ENCRYPTION_K,
                    false
                )
                AppPreferencesKeysMethods(context = applicationContext).saveBooleanToSharedPreferences(
                    AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_K,
                    true
                )
            }
        }
    }
}