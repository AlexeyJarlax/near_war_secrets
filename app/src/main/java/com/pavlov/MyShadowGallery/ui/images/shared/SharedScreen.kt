package com.pavlov.MyShadowGallery.ui.images.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.MyShadowGallery.ui.images.ImagesViewModel
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.theme.My3
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomCircularProgressIndicator
import com.pavlov.MyShadowGallery.theme.uiComponents.MatrixBackground
import com.pavlov.MyShadowGallery.theme.uiComponents.MyStyledDialog
import com.pavlov.MyShadowGallery.util.ToastExt
import timber.log.Timber

@Composable
fun SharedScreen(viewModel: ImagesViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val anImageWasSharedWithUsNow by viewModel.anImageWasSharedWithUsNow.collectAsState()
    val receivedfromoutside by viewModel.receivedFromOutside.collectAsState()
    val tempImages by viewModel.tempImages.collectAsState()
    var showImageDialog by remember { mutableStateOf(false) }
    val selectedUri by viewModel.selectedUri.collectAsState()
    val extractedUri by viewModel.extractedUri.collectAsState() // Получаем извлечённый URI
    val steganographyProgress by viewModel.steganographyProgress.collectAsState()

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
                        text = stringResource(R.string.shared_screen_no_images)
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
                                    val toastString = context.getString(R.string.shared_screen_error)
                                    ToastExt.show("$toastString: $clickedFileName")
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
                viewModel.clearExtractedUri()
            }
            showImageDialog = false
        }

        /** ----------- Обработка диалога для новых изображений, полученных через "Поделиться" ---------------*/
        if (showImageDialog && selectedUri != null && anImageWasSharedWithUsNow) {
            LaunchedEffect(selectedUri) {
                viewModel.saveBothImages(selectedUri!!) {
                    ToastExt.show(context.getString(R.string.shared_screen_save_success))
                    closeShareDialogWithMemoryWash()
                }
            }
            if (steganographyProgress.isNotEmpty()) {
                MyStyledDialog(onDismissRequest = {}) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(Color.Transparent),
                    ) {
                        Text(
                            text = stringResource(R.string.shared_screen_encryption_processing),
                            color = My3
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp)
                                .background(Color.Transparent)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            steganographyProgress.forEach { step ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cable,
                                        contentDescription = null,
                                        tint = My3,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = step,
                                        color = My3
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                            CustomCircularProgressIndicator()
                        }
                    }
                }
            }
        }

        /** ------------------- Обработка диалога для уже сохраненных изображений -----------------------*/
        if (showImageDialog && selectedUri != null && !anImageWasSharedWithUsNow) {
            SharedImageDialog(
                memeUri = selectedUri!!,
                extractedUri = extractedUri,
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
                onSave = {
                    viewModel.saveBothImages(selectedUri!!, onSaveComplete = {
                        ToastExt.show(context.getString(R.string.shared_screen_save_both_images))
                        viewModel.setAnImageWasSharedWithUsNow(false)
                        viewModel.clearSelectedUri()
                    })
                }
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
                val toastString = context.getString(R.string.shared_screen_error)
                ToastExt.show("$toastString: $latestTempImage")
            }
        }
    }
}
