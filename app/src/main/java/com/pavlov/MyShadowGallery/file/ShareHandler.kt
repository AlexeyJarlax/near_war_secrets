package com.pavlov.MyShadowGallery.file

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys
import com.pavlov.MyShadowGallery.util.AppPreferencesKeysMethods
import java.io.File
import java.io.FileOutputStream

class ShareHandler(private val context: Context) {

    fun handleSharedImage(uri: Uri) {
        showToast(context.getString(R.string.download))

        val folder = context.filesDir
        val fileName = NamingStyleManager(context).generateFileName(
            AppPreferencesKeysMethods(context).getBooleanFromSharedPreferences(
                AppPreferencesKeys.KEY_USE_THE_ENCRYPTION_K
            ), folder
        )
        val fileNameWithoutExtension = removeFileExtension(fileName)
        val outputFile = File(folder, "$fileNameWithoutExtension.share")

//        if (fileNameWithoutExtension.endsWith(".kk")) {
//            showToast("kk")
//            // Perform necessary actions for a file with ".kk" extension
//        } else {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
//            }
        }

        showToast(context.getString(R.string.done))
    }

    private fun removeFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf(".")
        return if (lastDotIndex == -1) {
            fileName
        } else {
            fileName.substring(0, lastDotIndex)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}