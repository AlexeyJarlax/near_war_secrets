package com.pavlov.MyShadowGallery

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import java.util.Date
import java.util.Locale

class StorageLog : AppCompatActivity() {

    private lateinit var scrollView: NestedScrollView
    private lateinit var logTextView: TextView
    private lateinit var back: Button // НАЗАД

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.storage_log)

        scrollView = findViewById(R.id.scrollView)
        logTextView = findViewById(R.id.logTextView)
        back = findViewById(R.id.button_back_from_gal)

        back.setOnClickListener {
            finish()
        }

        displayFileLog()
    }

    private fun displayFileLog() {
        val introMessage =
            "Журнал файлов, хранящихся на устройстве.\n" +
                    "Превью сохраняется с расширением p.\n" +
                    "Шифрованные файлы сохраняются с расширением .kk\n" +
                    "Сохранённые без шифрования - с расширением .o\n" +
                    "Расшифрованный файл, которым делится пользователь, существует в системе не продолжительное время с расширением .peekaboo\n\n"

        logTextView.text = introMessage

        val folder = applicationContext.filesDir
        val files = folder.listFiles()?.sortedBy { it.name }

        files?.forEach { file ->
            val fileName = file.name
            val lastModified = file.lastModified()
            val fileSizeInBytes = file.length()

            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(lastModified))

            val fileType = when {
                fileName.endsWith(".p", true) -> "Превью"
                fileName.endsWith(".kk", true) -> "Шифрованный файл"
                fileName.endsWith(".o", true) -> "Без шифрования"
                fileName.endsWith(".peekaboo", true) -> "Расшифрованный файл"
                else -> "Неизвестный тип"
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
    } }