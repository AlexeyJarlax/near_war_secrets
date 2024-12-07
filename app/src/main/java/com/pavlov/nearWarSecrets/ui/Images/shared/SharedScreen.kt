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
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    val temporaryImages by viewModel.receivedfromoutside.observeAsState(emptyList())
    var showImageDialog by remember { mutableStateOf(false) }
    val showSaveDialog by viewModel.showSaveDialog.observeAsState(false)
    val selectedUri by viewModel.selectedUri.collectAsState()

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
                                viewModel.setSelectedUri(clickedUri)
                                showImageDialog = true
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        /** ------------------ НИЖЕ ОСНОВНЫЕ ДИАЛОГИ ВСПЛЫВАЮЩИХ ОКОН -----------------------------------------*/
        fun closeShareDialogWithMemoryWash() {
            selectedUri?.let { uri ->
                viewModel.setAnImageWasSharedWithUsNow(false)
                viewModel.removeExtractedImage(uri)
                viewModel.deletePhoto(uri)
                viewModel.clearSelectedUri()
            }
            showImageDialog = false
        }

        if (showImageDialog && selectedUri != null && anImageWasSharedWithUsNow) { // в отношении изображений, полученных через поделиться
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
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
                        val success =
                            viewModel.saveExtractedImage(selectedUri!!, RECEIVED_FROM_OUTSIDE)
                        if (success) {
                            ToastExt.show("Сохранено")
                        } else {
                            ToastExt.show("Ошибка при сохранении")
                        }
                        closeShareDialogWithMemoryWash()
                    }
                )
                Icon( // индикатор диалога, с которым поделились
                    imageVector = Icons.Default.IosShare,
                    contentDescription = "Этим изображением поделились",
                    tint = Color.Green,
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp)
                        .graphicsLayer(rotationZ = -90f)
                )
            }
        }

        if (showImageDialog && selectedUri != null && !anImageWasSharedWithUsNow) {
            // в отношении изображений, по которым кликнул пользователь в списке уже сохраненных
            ImageDialog(
                uri = selectedUri!!,
                viewModel = viewModel,
                onDismiss = {
                    viewModel.clearSelectedUri()
                    showImageDialog = false
                },
                onDelete = {
                    viewModel.deletePhoto(selectedUri!!)
                    viewModel.clearSelectedUri()
                    showImageDialog = false
                },
                onSave = {}
            )
        }
    }

    // Отслеживаем добавление временных изображений
    LaunchedEffect(temporaryImages) {
        if (temporaryImages.isNotEmpty()) {
            viewModel.setSelectedUri(temporaryImages.last())
            showImageDialog = true
        }
    }
}
