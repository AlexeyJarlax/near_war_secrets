package com.pavlov.MyShadowGallery.util

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.pavlov.MyShadowGallery.R

fun Activity.showLoadingIndicator() {
    val buttonForCover2 = findViewById<View>(R.id.button_for_cover2) // кнопка заглушка экрана
    val loadingIndicator2 = findViewById<ProgressBar>(R.id.loading_indicator2) // индикатора загрузки
    buttonForCover2.visibility = View.VISIBLE
    loadingIndicator2.visibility = View.VISIBLE
}

fun Activity.hideLoadingIndicator(cornerLeft: Boolean) {
    val buttonForCover2 = findViewById<View>(R.id.button_for_cover2)
    val loadingIndicator2 = findViewById<ProgressBar>(R.id.loading_indicator2)

    val multiplier = if (cornerLeft) 3 else 1
    Handler(Looper.getMainLooper()).postDelayed({
        buttonForCover2.visibility = View.INVISIBLE
        loadingIndicator2.visibility = View.INVISIBLE
    }, APK.LOAD_PROCESSING_MILLISECONDS * multiplier)
}

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

