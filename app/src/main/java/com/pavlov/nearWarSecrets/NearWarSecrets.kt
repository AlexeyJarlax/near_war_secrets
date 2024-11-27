package com.pavlov.nearWarSecrets;

import android.app.Activity
import android.app.Application
import com.pavlov.nearWarSecrets.util.APK
import android.os.Bundle
import com.pavlov.nearWarSecrets.util.APKM
import com.pavlov.nearWarSecrets.util.ToastExt
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NearWarSecrets : Application() {
    private var cleanupDone = false

    override fun onCreate() {
        super.onCreate()
        ToastExt.init(this)
        applyDayNightTheme()

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
            name.endsWith(".peekaboo") || name.endsWith(".unknown") || name.endsWith(".k") || name.endsWith(".dat")  || name.endsWith(".profileInstalled")
        }

        peekabooFiles?.forEach { file ->
            file.delete()
        }

        // Проверка и удаление ключа шифрования, если флаг установлен
        val shouldDeleteEk = APKM(context = applicationContext).getBoolean(APK.KEY_DELETE_AFTER_SESSION, false)

        if (shouldDeleteEk) {
            }
        }

    private fun applyDayNightTheme() {
//        settings.applyTheme()
    }
    }
