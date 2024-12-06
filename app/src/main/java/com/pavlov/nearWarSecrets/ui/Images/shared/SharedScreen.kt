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
import com.pavlov.nearWarSecrets.ui.Images.ImageDialog
import java.io.File

@Composable
fun SharedScreen(
    viewModel: ImagesViewModel = hiltViewModel(),
    onImageClick: (Uri) -> Unit
) {
    val savedImages by viewModel.savedImages.observeAsState(emptyList())
    val temporaryImages by viewModel.extractedImages.observeAsState(emptyList())
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

            if (savedImages.isEmpty() && temporaryImages.isEmpty()) {
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
                    items(savedImages + temporaryImages) { uri ->
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
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Условный вызов ImageDialog вместо SharedDialog
        if (showDialog && selectedUri != null) {
            ImageDialog(
                uri = selectedUri!!,
                viewModel = viewModel,
                onDismiss = {
                    showDialog = false
                    // Решите, нужно ли удалять изображение при закрытии диалога
                    // Например, можно закомментировать следующую строку, если не требуется
                    // viewModel.removeExtractedImage(selectedUri!!)
                },
                onDelete = {
                    showDialog = false
                    viewModel.removeExtractedImage(selectedUri!!)
                }
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
