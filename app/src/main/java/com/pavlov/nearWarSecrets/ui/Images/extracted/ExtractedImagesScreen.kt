package com.pavlov.nearWarSecrets.ui.Images.extracted

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (extractedImages.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет полученных изображений",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(extractedImages) { uri ->
                        ExtractedImageItem(uri = uri)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}