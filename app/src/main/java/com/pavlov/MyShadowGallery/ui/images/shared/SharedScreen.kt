package com.pavlov.MyShadowGallery.ui.images.shared

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.MyShadowGallery.ui.images.ImagesViewModel
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.pavlov.MyShadowGallery.theme.uiComponents.MatrixBackground
import com.pavlov.MyShadowGallery.ui.images.ImageDialog
import com.pavlov.MyShadowGallery.util.ToastExt
import timber.log.Timber

@Composable
fun SharedScreen(viewModel: ImagesViewModel = hiltViewModel()) {
    val anImageWasSharedWithUsNow by viewModel.anImageWasSharedWithUsNow.collectAsState()
    val receivedfromoutside by viewModel.receivedFromOutside.collectAsState()
    val tempImages by viewModel.tempImages.collectAsState()
    var showImageDialog by remember { mutableStateOf(false) }
    val selectedUri by viewModel.selectedUri.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MatrixBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (receivedfromoutside.isEmpty()) {
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
                    items(receivedfromoutside.sortedByDescending { viewModel.getPhotoDate(it) }) { fileName ->
                        SharedItem(
                            fileName = fileName,
                            viewModel = viewModel,
                            onImageClick = { clickedFileName ->
                                val uri = viewModel.getFileUri(clickedFileName)
                                if (uri != null) {
                                    viewModel.setSelectedUri(uri)
                                    showImageDialog = true
                                } else {
                                    ToastExt.show("Не удалось получить URI для файла: $clickedFileName")
                                }
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
                viewModel.deletePhoto(uri)
                viewModel.clearSelectedUri()
            }
            viewModel.clearTempImages()
            showImageDialog = false
        }

        // Обработка диалога для новых изображений, полученных через "Поделиться"
        if (showImageDialog && selectedUri != null && anImageWasSharedWithUsNow) {
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
                        viewModel.saveSharedImage(selectedUri!!) { hiddenImageUri ->
                            if (hiddenImageUri != null) {
                                ToastExt.show("Сохранено скрытое изображение")
                            } else {
                                ToastExt.show("Сохранено обычное изображение")
                            }
                            viewModel.setAnImageWasSharedWithUsNow(false)
                            viewModel.clearSelectedUri()
                            viewModel.clearTempImages()
                        }
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

        // Обработка диалога для уже сохраненных изображений
        if (showImageDialog && selectedUri != null && !anImageWasSharedWithUsNow) {
            ImageDialog(
                uri = selectedUri!!,
                viewModel = viewModel,
                onDismiss = {
                    viewModel.clearSelectedUri()
                    showImageDialog = false
                    viewModel.clearTempImages()
                },
                onDelete = {
                    viewModel.deletePhoto(selectedUri!!)
                    viewModel.clearSelectedUri()
                    showImageDialog = false
                    viewModel.clearTempImages()
                },
                onSave = {}
            )
        }
    }

    LaunchedEffect(anImageWasSharedWithUsNow, tempImages) {
        if (anImageWasSharedWithUsNow && tempImages.isNotEmpty()) {
            Timber.d("=== Новый image был получен через 'Поделиться': ${tempImages.last()}")
            val latestTempImage = tempImages.last()
            val uri = viewModel.getFileUri(latestTempImage)
            if (uri != null) {
                viewModel.setSelectedUri(uri)
                showImageDialog = true
            } else {
                ToastExt.show("Не удалось получить URI для файла: $latestTempImage")
            }
        }
    }
}
