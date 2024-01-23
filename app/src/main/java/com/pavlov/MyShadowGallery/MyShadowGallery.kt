package com.pavlov.MyShadowGallery;

import android.app.Activity
import android.app.Application
import android.content.Context
import com.pavlov.MyShadowGallery.util.APK
import android.os.Bundle
import androidx.core.content.edit
import com.pavlov.MyShadowGallery.util.APKM

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
            name.endsWith(".peekaboo") || name.endsWith(".unknown") || name.endsWith(".k") || name.endsWith(".dat")  || name.endsWith(".profileInstalled")
        }

        peekabooFiles?.forEach { file ->
            file.delete()
        }

        // Проверка и удаление ключа шифрования, если флаг установлен
        val shouldDeleteEk = APKM(context = applicationContext).getBooleanFromSPK(APK.KEY_DELETE_AFTER_SESSION)

        if (shouldDeleteEk) {
            APKM(context = applicationContext).delMastersSecret(APK.KEY_BIG_SECRET1)
            APKM(context = applicationContext).delMastersSecret(APK.KEY_BIG_SECRET_NAME1)
            APKM(context = applicationContext).delMastersSecret(APK.KEY_BIG_SECRET2)
            APKM(context = applicationContext).delMastersSecret(APK.KEY_BIG_SECRET_NAME2)
            APKM(context = applicationContext).delMastersSecret(APK.KEY_BIG_SECRET3)
            APKM(context = applicationContext).delMastersSecret(APK.KEY_BIG_SECRET_NAME3)
            APKM(context = applicationContext).delFromSP(APK.DEFAULT_KEY)
            APKM(context = applicationContext).saveBooleanToSPK(APK.KEY_USE_THE_ENCRYPTION_K, true)
            APKM(context = applicationContext).saveBooleanToSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, false)
            }
        }
    }
