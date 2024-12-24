package com.pavlov.MyShadowGallery

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.pavlov.MyShadowGallery.data.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val imageRepository: ImageRepository
) : DefaultLifecycleObserver {

    private val TAG = "AppLifecycleObserver"

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "App in background. Clearing temp images.")
        owner.lifecycleScope.launch(Dispatchers.IO) {
            imageRepository.clearTempImages()
        }
    }
}