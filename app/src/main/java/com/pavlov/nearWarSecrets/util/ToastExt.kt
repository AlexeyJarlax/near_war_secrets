package com.pavlov.nearWarSecrets.util

import android.R
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Keep

@Keep
object ToastExt {

    private lateinit var appContext: Context
    private val mainHandler = Handler(Looper.getMainLooper())

    fun init(application: Application) {
        appContext = application.applicationContext
    }

    fun show(message: String, duration: Int = Toast.LENGTH_LONG) {
        mainHandler.post {
            val toast = Toast.makeText(appContext, message, duration)
            val toastView = toast.view
            val toastMessage = toastView?.findViewById<TextView>(R.id.message)
            toastMessage?.let {
                it.setTextColor(Color.BLACK)
                it.setBackgroundColor(Color.WHITE)
            }
            toast.show()
        }
    }
}


/** применение:
 * контекст для вьюмодели:
@ApplicationContext private val context: Context,

* контекст для экрана:
val context = LocalContext.current

ToastExt.show(context.getString(R.string.redirecting)) // для компоуз и других областей работает одинаково

private val defaultLocation = getDefaultLocation()

 */