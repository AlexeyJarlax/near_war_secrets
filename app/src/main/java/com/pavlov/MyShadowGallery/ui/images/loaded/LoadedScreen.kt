package com.pavlov.MyShadowGallery.ui.images.loaded

import androidx.compose.foundation.lazy.grid.items
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.pavlov.MyShadowGallery.util.ToastExt
import java.io.File
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.theme.My7
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomButtonOne
import com.pavlov.MyShadowGallery.ui.images.ImageDialog
import com.pavlov.MyShadowGallery.ui.images.ImagesViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import com.pavlov.MyShadowGallery.theme.uiComponents.MatrixBackground
import com.pavlov.MyShadowGallery.util.APK.TEMP_IMAGES

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoadedScreen(
    viewModel: ImagesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uploadedbyme by viewModel.uploadedByMe.collectAsState()
    var showImageDialog by remember { mutableStateOf(false) }
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()
    val selectedUri by viewModel.selectedUri.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPreviewVisible by rememberSaveable { mutableStateOf(false) }
    val cameraSelector by viewModel.cameraSelector.collectAsState()
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }
    val isStorageMode = false

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isPreviewVisible = true
        } else {
            ToastExt.show(context.getString(R.string.error_camera))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addPhoto(it)
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            ToastExt.show(context.getString(R.string.error_gallery))
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.8f),
                elevation = 8.dp
            ) {

                /** ---------ПАНЕЛЬ УПРАВЛЕНИЯ С КНОПКАМИ: ФОТО, ГАЛЕРЕЯ (СНЯТЬ И РАЗВЕРНУТЬ ДЛЯ КАМЕРЫ) ------------------------------------------*/
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isPreviewVisible) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CustomButtonOne(
                                onClick = {
                                    val fileName = viewModel.getFileName()
                                    val photoListDir = File(context.filesDir, TEMP_IMAGES)
                                    if (!photoListDir.exists()) {
                                        photoListDir.mkdirs()
                                    }
                                    val file = File(photoListDir, fileName)
                                    val outputOptions =
                                        ImageCapture.OutputFileOptions.Builder(file)
                                            .build()

                                    imageCapture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                viewModel.setSelectedUri(file.toUri())
                                                isPreviewVisible = false
                                                viewModel.onSavePhotoClicked(true)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                ToastExt.show(context.getString(R.string.error_save))
                                            }
                                        }
                                    )
                                    isPreviewVisible = false
                                },
                                text = context.getString(R.string.snapshot),
                                textColor = My7,
                                iconColor = My7,
                                icon = Icons.Default.AddAPhoto
                            )

                            CustomButtonOne(
                                onClick = {
                                    viewModel.switchCamera()
                                },
                                text = context.getString(R.string.flip),
                                textColor = My7,
                                iconColor = My7,
                                icon = Icons.Default.Cameraswitch
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CustomButtonOne(
                            onClick = {
                                if (isPreviewVisible) {
                                    isPreviewVisible = false
                                } else {
                                    if (ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        isPreviewVisible = true
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                            text = if (isPreviewVisible)
                                context.getString(R.string.cam_bat_hide)
                            else
                                context.getString(R.string.cam_bat_start),
                            textColor = My7,
                            iconColor = My7,
                            icon = if (isPreviewVisible)
                                Icons.Default.HideImage
                            else
                                Icons.Default.PhotoCamera,
                        )

                        CustomButtonOne(
                            onClick = {
                                val permission =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    } else {
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    }
                                if (ContextCompat.checkSelfPermission(
                                        context, permission
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    galleryPermissionLauncher.launch(permission)
                                }
                            },
                            text = context.getString(R.string.galery),
                            textColor = My7,
                            iconColor = My7,
                            icon = Icons.Default.InsertPhoto
                        )
                    }
                }
            }
        },

        /** ----------------------------------------ОСНОВНОЙ КОНТЕНТ ---------------------------------------------------------------------------*/
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                MatrixBackground()

                /** ----------------------------------------СПИСОК ФОТО или заглушка -----------------------------------------------------------*/
                if (uploadedbyme.isEmpty()) {
                    Text(
                        text = "Нет добавленных изображений",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uploadedbyme.sortedByDescending { viewModel.getPhotoDate(it) }) { fileName ->
                            LoadedItem(
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

                /** ----------------------------------------ВИДЕОИСКАТЕЛЬ КАМЕРЫ ТЕЛЕФОНА ---------------------------------------------------*/
                if (isPreviewVisible && !isStorageMode) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .border(
                                width = 2.dp,
                                color = Color.Yellow,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                /** ----------------------------------------ВЫЗОВЫ ДИАЛОГОВЫХ ОКОН --------------------------------------------------------------------*/

                if (showSaveDialog && selectedUri != null) {
                    ImageDialog( // клик по snapshot
                        uri = selectedUri!!,
                        viewModel = viewModel,
                        onDismiss = {
                            viewModel.deletePhoto(selectedUri!!)
                            viewModel.clearSelectedUri()
                            viewModel.onSavePhotoClicked(false)
                                    },
                        onDelete = {
                            viewModel.deletePhoto(selectedUri!!)
                            viewModel.clearSelectedUri()
                            viewModel.onSavePhotoClicked(false)
                        },
                        isItNew = true,
                        onSave = {
                            viewModel.addPhoto(selectedUri!!)
                            viewModel.clearSelectedUri()
                        }

                    )
                }

                if (showImageDialog && selectedUri != null) {
                    ImageDialog( // клик по фоткам в списке сохраненных
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
        }
    )

    LaunchedEffect(isPreviewVisible, cameraSelector) {
        if (isPreviewVisible) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector as CameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                ToastExt.show(context.getString(R.string.error_camera))
            }
        }
    }
}
