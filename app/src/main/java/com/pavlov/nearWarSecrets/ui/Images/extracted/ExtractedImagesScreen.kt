package com.pavlov.nearWarSecrets.ui.Images.extracted

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.ui.Images.ImagesViewModel

@Composable
fun ExtractedImagesScreen(
    viewModel: ImagesViewModel = hiltViewModel()
) {
    // Список временных изображений
    val extractedImages by viewModel.extractedImages.observeAsState(emptyList())

    // Список сохраненных изображений
    val savedImages by viewModel.savedImages.observeAsState(emptyList())

    // Состояние для выбранного изображения
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Отслеживание новых временных изображений и открытие диалога
    LaunchedEffect(extractedImages) {
        if (extractedImages.isNotEmpty()) {
            selectedImageUri = extractedImages.first()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MatrixBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Отображение сохраненных изображений
            if (savedImages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет сохраненных изображений",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(savedImages) { uri ->
                        SavedImageItem(uri = uri)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Отображение диалога при наличии временных изображений
        selectedImageUri?.let { uri ->
            ExtractedImagesDialog(
                uri = uri,
                onDismiss = { selectedImageUri = null },
                viewModel = viewModel
            )
        }
    }
}
