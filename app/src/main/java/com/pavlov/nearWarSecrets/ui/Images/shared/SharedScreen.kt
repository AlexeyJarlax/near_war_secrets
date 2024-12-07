package com.pavlov.nearWarSecrets.ui.Images.shared

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.pavlov.nearWarSecrets.util.APK.RECEIVED_FROM_OUTSIDE
import com.pavlov.nearWarSecrets.util.ToastExt

@Composable
fun SharedScreen(
    viewModel: ImagesViewModel = hiltViewModel(),
    onImageClick: (Uri) -> Unit
) {
    val anImageWasSharedWithUsNow by viewModel.anImageWasSharedWithUsNow.collectAsState()
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
                        text = "Нет сохранённых изображений"
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

        /** ------------------ НИЖЕ ОСНОВНЫЕ ДИАЛОГИ ВСПЛЫВАЮЩИХ ОКОН -----------------------------------------*/
        fun closeShareDialogWithMemoryWash() {
            showDialog = false
            viewModel.removeExtractedImage(selectedUri!!)
            viewModel.setAnImageWasSharedWithUsNow(false)
        }

        if (showDialog && temporaryImages != null && anImageWasSharedWithUsNow) { // в отношении изображений, полученных через поделиться
            ImageDialog(
                uri = selectedUri!!,
                viewModel = viewModel,
                onDismiss = {
                    closeShareDialogWithMemoryWash()
                },
                onDelete = {
                    closeShareDialogWithMemoryWash()
                },
                isItNew = true,
                onSave = {
                    val success = viewModel.saveExtractedImage(selectedUri!!, RECEIVED_FROM_OUTSIDE)
                    if (success) {
                        ToastExt.show("Сохранено")
                    } else {
                        ToastExt.show("Ошибка при сохранении")
                    }
                    closeShareDialogWithMemoryWash()
                }
            )
        }

        if (showDialog && selectedUri != null && !anImageWasSharedWithUsNow) { // в отношении изображений, по которым кликнул пользователь в списке уже сохраненных
            ImageDialog(
                uri = selectedUri!!,
                viewModel = viewModel,
                onDismiss = {
                    showDialog = false
                },
                onDelete = {
                    showDialog = false
                },
                onSave = {}
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
