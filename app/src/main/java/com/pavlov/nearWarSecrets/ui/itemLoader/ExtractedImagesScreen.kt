package com.pavlov.nearWarSecrets.ui.itemLoader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter

/**
 * Экран для отображения извлеченных оригинальных изображений.
 *
 * @param extractedImages Список URI извлеченных изображений.
 * @param onDismiss Функция для закрытия экрана.
 */
@Composable
fun ExtractedImagesScreen(
    extractedImages: List<Uri>,
    onDismiss: () -> Unit
) {
    // Получение контекста для запуска Intent и отображения Toast
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Регулируйте высоту по необходимости
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Извлеченные изображения",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(extractedImages) { uri ->
                        ExtractedImageItem(uri = uri)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Закрыть")
                }
            }
        }
    }
}



