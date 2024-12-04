package com.pavlov.nearWarSecrets.ui.Images.loaded

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pavlov.nearWarSecrets.util.ToastExt
import java.io.File
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.theme.My7
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground
import com.pavlov.nearWarSecrets.ui.Images.ImageDialog
import com.pavlov.nearWarSecrets.ui.Images.ImagesViewModel
import com.pavlov.nearWarSecrets.ui.Images.PhotoItem
import com.pavlov.nearWarSecrets.ui.Images.extracted.ExtractedImagesDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemLoaderScreen(
    viewModel: ImagesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val photoList by viewModel.photoList.observeAsState(emptyList())
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }
    val showSaveDialog by viewModel.showSaveDialog.observeAsState(false)
    var selectedUri: Uri? by remember { mutableStateOf(null) }
    val isStorageMode = false
    val isLoading by viewModel.isLoading.observeAsState(false)
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPreviewVisible by remember { mutableStateOf(false) }
    val cameraSelector by viewModel.cameraSelector.observeAsState(CameraSelector.DEFAULT_BACK_CAMERA)
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    val extractedImages by viewModel.extractedImages.observeAsState(emptyList())
    var showExtractedImagesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(extractedImages) {
        if (extractedImages.isNotEmpty()) {
            showExtractedImagesDialog = true
        }
    }

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
            selectedUri = it
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

    Scaffold(
        content = { padding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                MatrixBackground()

                // Центрируем AndroidView по вертикали
                if (isPreviewVisible && !isStorageMode) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .align(Alignment.Center)
                    )
                }

                // Размещаем список фотографий или заглушку
                if (photoList.isEmpty()) {
                    Text(
                        text = "Нет добавленных изображений",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        items(photoList) { fileName ->
                            PhotoItem(
                                fileName = fileName,
                                viewModel = viewModel,
                                onImageClick = {
                                    selectedFileName = it
                                    showImageDialog = true
                                }
                            )
                        }
                    }
                }

                // Размещаем кнопки управления внизу экрана
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
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
                                    val file = File(context.filesDir, fileName)
                                    val outputOptions =
                                        ImageCapture.OutputFileOptions.Builder(file)
                                            .build()

                                    imageCapture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                selectedUri = file.toUri()
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
                                    isPreviewVisible = false
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

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                if (showImageDialog && selectedFileName != null) {
                    ImageDialog(
                        fileName = selectedFileName!!,
                        viewModel = viewModel,
                        onDismiss = { showImageDialog = false },
                        onDelete = {
                            viewModel.deletePhoto(selectedFileName!!)
                            showImageDialog = false
                        }
                    )
                }

                if (showSaveDialog && selectedUri != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onSavePhotoClicked(false) },
                        title = { Text("Сохранить или поделиться изображением?") },
                        text = { Text("Выберите действие для изображения.") },
                        confirmButton = {
                            CustomButtonOne(
                                onClick = {
                                    viewModel.savePhotoWithChoice(selectedUri!!)
                                },
                                text = context.getString(R.string.save),
                                icon = Icons.Default.NoEncryption
                            )
                        },
                        dismissButton = {
                            CustomButtonOne(
                                onClick = {
                                    // Реализуйте логику для поделиться изображением
                                },
                                text = context.getString(R.string.share_the_img),
                                icon = Icons.Default.Share
                            )
                        }
                    )
                }

                if (extractedImages.isNotEmpty() && showExtractedImagesDialog) {
                    ExtractedImagesDialog(
                        extractedImages = extractedImages,
                        onDismiss = {
                            showExtractedImagesDialog = false
                            viewModel.clearExtractedImages()
                        }
                    )
                }
            }
        }
    )
}