package com.pavlov.nearWarSecrets.ui.Images.shared

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground
import com.pavlov.nearWarSecrets.ui.Images.ImagesViewModel
import android.net.Uri

@Composable
fun SharedScreen(
    viewModel: ImagesViewModel = hiltViewModel(),
    onImageClick: (Uri) -> Unit
) {
    val savedImages by viewModel.savedImages.observeAsState(emptyList())
    val temporaryImages by viewModel.extractedImages.observeAsState(emptyList()) // Добавлено
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MatrixBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (savedImages.isEmpty() && temporaryImages.isEmpty()) { // Изменено
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет сохранённых изображений",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onBackground
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedImages + temporaryImages) { uri -> // Объединяем списки
                        SharedItem(
                            uri = uri,
                            viewModel = viewModel,
                            onImageClick = { clickedUri ->
                                selectedUri = clickedUri
                                showDialog = true
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Условный вызов SharedDialog
        if (showDialog && selectedUri != null) {
            SharedDialog(
                uri = selectedUri!!,
                onDismiss = {
                    showDialog = false
                    viewModel.removeExtractedImage(selectedUri!!) // Удаляем временное изображение после закрытия диалога
                },
                viewModel = viewModel
            )
        }
    }

    // Отслеживаем добавление временных изображений
    LaunchedEffect(temporaryImages) {
        if (temporaryImages.isNotEmpty()) {
            selectedUri = temporaryImages.last()
            showDialog = true
        }
    }
}
