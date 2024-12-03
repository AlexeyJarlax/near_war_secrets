package com.pavlov.nearWarSecrets;

import android.app.Application
import com.pavlov.nearWarSecrets.util.ToastExt
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.pavlov.nearWarSecrets.util.ReleaseTree

@HiltAndroidApp
class NearWarSecrets : Application() {

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


