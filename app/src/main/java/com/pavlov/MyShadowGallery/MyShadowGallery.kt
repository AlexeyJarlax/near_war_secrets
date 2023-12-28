package com.pavlov.MyShadowGallery;

import android.app.Activity
import android.app.Application;
import android.os.Bundle

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
            name.endsWith(".peekaboo") || name.endsWith(".unknown") || name.endsWith(".k")
        }

        peekabooFiles?.forEach { file ->
            file.delete()
        }
    }
}