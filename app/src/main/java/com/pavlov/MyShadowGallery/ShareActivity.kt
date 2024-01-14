package com.pavlov.MyShadowGallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toUri
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.AppPreferencesKeysMethods
import com.pavlov.MyShadowGallery.util.FileHandler
import com.pavlov.MyShadowGallery.util.NamingStyleManager
import com.pavlov.MyShadowGallery.util.ThemeManager
import java.io.File
import java.io.FileOutputStream

class ShareActivity : AppCompatActivity() {

    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        fileHandler = FileHandler(this)

        if (Intent.ACTION_SEND == intent.action && intent.type?.startsWith("image/") == true) {
            // Получаем URI изображения
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            // Проверяем, что URI не является null
            if (imageUri != null) {
                fileHandler.handleSharedImage(imageUri)
            }
        }
        finish()
    }
}

//class ShareActivity : AppCompatActivity() {
//
////    private lateinit var encryption: Encryption
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_share)
//        val videoView = findViewById<VideoView>(R.id.videoView0)
//        val themeManager = ThemeManager
////        encryption = Encryption(this@ShareActivity)
//        var backgroundView = findViewById<ImageView>(R.id.background_image)
//        backgroundView.setImageResource(ThemeManager.applyUserSwitch(this))
//
//        if (themeManager.isNightModeEnabled(this)) {// применяем тему в старте: ночная
//                videoView.alpha = 0.5f
//                backgroundView.alpha = 0.0f
//                videoView.setVideoURI(Uri.parse("android.resource://${packageName}/${R.raw.murcat}"))
//                videoView.setOnPreparedListener { mediaPlayer ->
//                    mediaPlayer.isLooping = true
//                    mediaPlayer.setVolume(0.0f, 0.0f)
//                    mediaPlayer.start()
//                }
//            }
//
//        // Проверяем, был ли передан файл через Intent
//        if (Intent.ACTION_SEND == intent.action && intent.type?.startsWith("image/") == true) {
//            handleSharedImage(intent) // Обрабатываем переданный файл
//        }
//    }
//
//    private fun handleSharedImage(intent: Intent) {
//        // Получаем URI изображения
//        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
//        // Проверяем, что URI не является null
//        if (imageUri != null) {
//            handleSelectedImage(imageUri)
//        }
//    }
//
//    private fun handleSelectedImage(uri: Uri) {
//        showToast(getString(R.string.download))
//
//        val folder = applicationContext.filesDir
//        var existOrNot: Boolean = AppPreferencesKeysMethods(context = this).getBooleanFromSharedPreferences(
//            AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_K)
//
//        val fileName = NamingStyleManager(application).generateFileName(existOrNot, folder)
//
//        val outputFile = File(folder, fileName)
//
//        // Проверяем расширение файла
//        if (fileName.endsWith(".kk")) {
//            showToast("kk")
//            // Здесь вы можете выполнить необходимые действия для файла с расширением "kk"
//        } else {
//            contentResolver.openInputStream(uri)?.use { input ->
//                FileOutputStream(outputFile).use { output ->
//                    input.copyTo(output)
//                }
//            }
//
////            if (AppPreferencesKeysMethods(context = this).getBooleanFromSharedPreferences(
////                    AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_K)) {
////                encryption.createThumbnail(this@ShareActivity, outputFile.toUri())
////                encryption.encryptImage(outputFile.toUri(), fileName)
////            } else {
//                showToast(getString(R.string.enception_no))
////                encryption.addPhotoToList(0, outputFile.toUri())
////                notifyDSC()
////            }
//        }
//        showToast(getString(R.string.done))
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    // Метод для отображения индикатора загрузки
////    fun showLoadingIndicator() {
////        //        frameLayout2.setEnabled(false)
////        buttonForCover2.visibility = View.VISIBLE
////        loadingIndicator2.visibility = View.VISIBLE
////    }
//}
