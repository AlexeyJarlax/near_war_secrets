package com.pavlov.MyShadowGallery

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.pavlov.MyShadowGallery.util.ToastExt
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.pavlov.MyShadowGallery.util.ReleaseTree
import jakarta.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        ToastExt.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }
}