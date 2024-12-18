package com.pavlov.MyShadowGallery

import android.app.Application
import com.pavlov.MyShadowGallery.util.ToastExt
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.pavlov.MyShadowGallery.util.ReleaseTree

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ToastExt.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }
}