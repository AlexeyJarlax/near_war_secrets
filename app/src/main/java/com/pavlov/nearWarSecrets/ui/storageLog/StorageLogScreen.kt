package com.pavlov.nearWarSecrets.ui.storageLog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.ui.theme.uiComponents.MatrixBackground
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StorageLogScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val logEntries = remember { mutableStateListOf<String>() }

    // Загрузка данных при первом запуске
    LaunchedEffect(Unit) {
        val introMessage = context.getString(R.string.storage_log_activity)
        logEntries.add(introMessage)

        val folder = context.filesDir
        val files = folder.listFiles()
            ?.filter { it.extension != "dat" && it.name != "profileInstalled" }
            ?.sortedBy { it.name }

        files?.forEach { file ->
            val fileName = file.name
            val lastModified = file.lastModified()
            val fileSizeInBytes = file.length()

            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(lastModified))

            val fileType = when {
                fileName.endsWith(".p", true) -> context.getString(R.string.preview)
                fileName.endsWith(".kk", true) -> context.getString(R.string.encrypted_file)
                fileName.endsWith(".o", true) -> context.getString(R.string.whithout_enception)
                fileName.endsWith(".peekaboo", true) -> context.getString(R.string.decripted_file)
                fileName.endsWith(".share", true) -> context.getString(R.string.file_from_web)
                else -> context.getString(R.string.unnown_tipe)
            }

            val fileSizeInMegabytes = fileSizeInBytes.toDouble() / (1024 * 1024)
            val formattedFileSize = String.format("%.2f", fileSizeInMegabytes)

            val logEntry = """
                ${context.getString(R.string.file_type)}: $fileType
                ${context.getString(R.string.file_name)}: $fileName
                ${context.getString(R.string.date)}: $formattedDate
                ${context.getString(R.string.size)}: $formattedFileSize MB
                
            """.trimIndent()

            logEntries.add(logEntry)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MatrixBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(text = context.getString(R.string.button_back))
        }

        Spacer(modifier = Modifier.height(16.dp))

        SelectionContainer {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                logEntries.forEach { entry ->
                    Text(text = entry)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}}