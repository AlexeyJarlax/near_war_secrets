package com.pavlov.MyShadowGallery.file

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.pavlov.MyShadowGallery.R
import java.util.Date
import java.util.Locale

    class StorageLogActivity : AppCompatActivity() {

        private lateinit var scrollView: NestedScrollView
        private lateinit var logTextView: TextView
        private lateinit var back: Button // НАЗАД

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_storage_log)

            scrollView = findViewById(R.id.scrollView)
            logTextView = findViewById(R.id.logTextView)
            back = findViewById(R.id.button_back_from_gal)

            back.setOnClickListener {
                finish()
            }

            displayFileLog()
        }

        private fun displayFileLog() {
            val introMessage = getString(R.string.storage_log_activity)

            logTextView.text = introMessage

            val folder = applicationContext.filesDir
            val files = folder.listFiles()?.filter { it.extension != "dat" && it.name != "profileInstalled" }?.sortedBy { it.name }

            files?.forEach { file ->
                val fileName = file.name
                val lastModified = file.lastModified()
                val fileSizeInBytes = file.length()

                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(lastModified))

                val fileType = when {
                    fileName.endsWith(".p", true) -> getString(R.string.preview)
                    fileName.endsWith(".kk", true) -> getString(R.string.encrypted_file)
                    fileName.endsWith(".o", true) -> getString(R.string.whithout_enception)
                    fileName.endsWith(".peekaboo", true) -> getString(R.string.decripted_file)
                    fileName.endsWith(".share", true) -> getString(R.string.file_from_web)
                    else -> getString(R.string.unnown_tipe)
                }

                val fileSizeInMegabytes = fileSizeInBytes.toDouble() / (1024 * 1024)
                val formattedFileSize = String.format("%.2f", fileSizeInMegabytes)

                val logEntry =
                    "File Type: $fileType\n" +
                            "File Name: $fileName\n" +
                            "Date: $formattedDate\n" +
                            "Size: $formattedFileSize MB\n\n"

                logTextView.append(logEntry)
            }
        }
    }