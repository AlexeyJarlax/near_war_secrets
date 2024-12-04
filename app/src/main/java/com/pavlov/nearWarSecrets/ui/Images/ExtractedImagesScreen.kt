package com.pavlov.nearWarSecrets.ui.Images

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground
import java.io.File

@Composable
fun ExtractedImagesScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    // Получаем список файлов из приватной директории
    val imagesDir = File(context.filesDir, "ExtractedImages")
    val extractedImages = imagesDir.listFiles()?.map { Uri.fromFile(it) } ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        MatrixBackground()
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            text = "Извлеченные изображения",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (extractedImages.isEmpty()) {
            Text(
                text = "Нет полученных изображений",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(extractedImages) { uri ->
                    ExtractedImageItem(uri = uri)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}}