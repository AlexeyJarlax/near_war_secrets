package com.pavlov.MyShadowGallery.ui.storageLog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.theme.uiComponents.MatrixBackground
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StorageLogScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val logEntries = remember { mutableStateListOf<LogEntry>() }

    val directories = listOf(
        "uploadedbyme" to "Uploaded by Me",
        "receivedfromoutside" to "Received from Outside",
        "tempimages" to "Temporary Images",
        "cache" to "Cache"
    )

    LaunchedEffect(Unit) {
        val introMessage = ""
        logEntries.add(LogEntry.Text(introMessage))

        directories.forEach { (dirName, displayName) ->
            logEntries.add(LogEntry.Header("Directory: $displayName"))

            val directory = context.filesDir.resolve(dirName)
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.forEach { file ->
                    val fileName = file.name
                    val lastModified = file.lastModified()
                    val fileSizeInBytes = file.length()

                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru"))
                    val formattedDate = dateFormat.format(Date(lastModified))

                    val fileSizeInMegabytes = fileSizeInBytes.toDouble() / (1024 * 1024)
                    val formattedFileSize = String.format("%.2f", fileSizeInMegabytes)

                    val logEntry = """
                        ${context.getString(R.string.file_name)}: $fileName
                        ${context.getString(R.string.file_path)}: ${file.absolutePath}
                        ${context.getString(R.string.date)}: $formattedDate
                        ${context.getString(R.string.size)}: $formattedFileSize MB
                    """.trimIndent()

                    logEntries.add(LogEntry.Text(logEntry))
                }
            } else {
                logEntries.add(LogEntry.Text(context.getString(R.string.directory_empty)))
            }
        }
    }

    Scaffold(
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MatrixBackground()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Text(
                        text = context.getString(R.string.storage_log_activity),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .padding(20.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .fillMaxWidth()
                        ) {
                            logEntries.forEach { entry ->
                                when (entry) {
                                    is LogEntry.Text -> {
                                        Text(
                                            text = entry.content,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    is LogEntry.Header -> {
                                        Text(
                                            text = entry.content,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

sealed class LogEntry {
    data class Text(val content: String) : LogEntry()
    data class Header(val content: String) : LogEntry()
}