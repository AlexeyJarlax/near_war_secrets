package com.pavlov.nearWarSecrets.ui.itemLoader

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
import androidx.compose.material.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemLoaderScreen(
    viewModel: ItemLoaderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val showSaveDialog by viewModel.showSaveDialog.observeAsState(false)
    var selectedUri: Uri? by remember { mutableStateOf(null) }
    val isStorageMode = false
    val photoList by viewModel.photoList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPreviewVisible by remember { mutableStateOf(false) }
    val cameraSelector by viewModel.cameraSelector.observeAsState(CameraSelector.DEFAULT_BACK_CAMERA)
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    // Лаунчер разрешений для камеры
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isPreviewVisible = true
        } else {
            ToastExt.show(context.getString(R.string.error_camera))
        }
    }

    // Лаунчер для галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addPhoto(it)
        }
    }

    // Лаунчер разрешений для галереи
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            ToastExt.show(context.getString(R.string.error_gallery))
        }
    }

    // Запуск камеры и настройка превью
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
            Box(modifier = Modifier.fillMaxSize()) {
                MatrixBackground()
                Column(modifier = Modifier.fillMaxSize()) {
                    // Отображение списка фотографий
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .weight(1f)
                            .padding(padding)
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

                    // Отображение превью камеры
                    if (isPreviewVisible && !isStorageMode) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }

                    // Кнопки управления
                    if (!isStorageMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = {
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
                            }) {
                                Text(
                                    text = if (isPreviewVisible)
                                        context.getString(R.string.cam_bat_hide)
                                    else
                                        context.getString(R.string.cam_bat_start)
                                )
                            }

                            Button(onClick = {
                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                            }) {
                                Text(text = context.getString(R.string.galery))
                            }

                            if (isPreviewVisible) {
                                Button(onClick = {
                                    val fileName = viewModel.getFileName()
                                    val file = File(context.filesDir, fileName)
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

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
                                }) {
                                    Text(text = context.getString(R.string.snapshot))
                                }

                                Button(onClick = {
                                    viewModel.switchCamera()
                                }) {
                                    Text(text = context.getString(R.string.flip))
                                }
                            }
                        }
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
                        },
                        onShare = {
                            viewModel.shareImage(selectedFileName!!)
                            showImageDialog = false
                        }
                    )
                }

                // Диалог для сохранения с шифрованием
                if (showSaveDialog && selectedUri != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onSavePhotoClicked(false) },
                        title = { Text("Выбор шифрования") },
                        text = { Text("Сохранить фотографию с шифрованием или без?") },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.savePhotoWithChoice(selectedUri!!, encrypt = true)
                            }) {
                                Text("С шифрованием")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                viewModel.savePhotoWithChoice(selectedUri!!, encrypt = false)
                            }) {
                                Text("Без шифрования")
                            }
                        }
                    )
                }
            }
        }
    )
}