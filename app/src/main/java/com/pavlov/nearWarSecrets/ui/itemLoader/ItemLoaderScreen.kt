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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemLoaderScreen(
    viewModel: ItemLoaderViewModel = hiltViewModel()
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
            selectedUri = uri
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

                    Column( // Кнопки управления
                        modifier = Modifier
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (isPreviewVisible) {
                            Row( //* верхняя строка: снимок и ротация (активна в момент видеоискателя)
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
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


                        Row( //* нижняя строка: камера и галерея
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
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

                LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                    items(photoList) { fileName ->
                        PhotoItem(
                            fileName = fileName,
                            viewModel = viewModel,
                            onImageClick = {
                                selectedFileName = fileName
                                showImageDialog = true
                            }
                        )
                    }
                }

                // Диалог для сохранения с шифрованием
                if (showSaveDialog && selectedUri != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onSavePhotoClicked(false) },
                        title = { Text("Save or Share Image?") },
                        text = { Text("Choose an action for the image.") },
                        confirmButton = {
                            CustomButtonOne(
                                onClick = {
                                    viewModel.savePhotoWithChoice(selectedUri!!)
                                },
                                text = context.getString(R.string.save), // Changed button text
                                icon = Icons.Default.NoEncryption
                            )
                        },
                        dismissButton = {
                            CustomButtonOne(
                                onClick = {
//                                    viewModel.shareImage(fileName)
//                                    viewModel.onSavePhotoClicked(false)
                                },
                                text = context.getString(R.string.share_the_img),
                                icon = Icons.Default.Share
                            )
                        }
                    )
                }
            }
        }
    )
}