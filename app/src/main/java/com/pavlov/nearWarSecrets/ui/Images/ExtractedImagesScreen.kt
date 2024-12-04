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
    val context = LocalContext.current
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
    }
}



