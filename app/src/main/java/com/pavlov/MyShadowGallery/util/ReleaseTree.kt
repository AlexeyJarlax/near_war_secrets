package com.pavlov.MyShadowGallery.util

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val messageA = "== $message"
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        if (t != null) {
            Log.e(tag, messageA, t)
        } else {
            Log.println(priority, tag, messageA)
        }
    }
}