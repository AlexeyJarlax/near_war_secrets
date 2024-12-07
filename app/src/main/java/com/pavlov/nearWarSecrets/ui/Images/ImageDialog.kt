package com.pavlov.nearWarSecrets.ui.Images

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomCircularProgressIndicator
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
import com.pavlov.nearWarSecrets.ui.Images.loaded.MemeSelectionDialog

    /** ИСПОЛЬЗУЮ ЭТОТ ЭКРАН НА ВСЕ ВАРИАНТЫ ОТКРЫТИЯ ИЗОБРАЖЕНИЙ: ПОЛУЧЕННОЕ ВНЕШНЕ ИЛИ ОТКРЫТОЕ ИЗ ХРАНИЛИЩА*/
    @Composable
    fun ImageDialog(
        uri: Uri,
        viewModel: ImagesViewModel,
        onDismiss: () -> Unit,
        onDelete: () -> Unit,
        isItNew: Boolean = false,
        onSave: (() -> Unit)? = null
    ) {
        val context = LocalContext.current
        val actualImageFile = viewModel.uriToFile(uri)

        if (actualImageFile == null || !actualImageFile.exists()) {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
            return
        }

        val date = viewModel.getPhotoDate(actualImageFile.name)
        val name = viewModel.getFileNameWithoutExtension(actualImageFile.name)
        val actualUri = Uri.fromFile(actualImageFile)

        var showShareOptions by remember { mutableStateOf(false) }
        var showMemeSelection by remember { mutableStateOf(false) }
        var isProcessing by remember { mutableStateOf(false) }

        MyStyledDialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = name, style = MaterialTheme.typography.h6)
                Text(text = date, style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.height(8.dp))

                ZoomableImage(
                    uri = actualUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CustomButtonOne(
                        onClick = { showShareOptions = true },
                        text = "Поделиться",
                        icon = Icons.Default.Share
                    )

                    if (isItNew) {
                        CustomButtonOne(
                            onClick = {
                                onSave?.invoke()
                                onDismiss()
                            },
                            text = "Сохранить",
                            icon = Icons.Default.Save
                        )
                    }

                    CustomButtonOne(
                        onClick = onDelete,
                        text = "Удалить",
                        icon = Icons.Default.Delete
                    )

                    CustomButtonOne(
                        onClick = onDismiss,
                        text = "Закрыть",
                        icon = Icons.Default.Close
                    )
                }
            }
        }

// Диалог выбора способа поделиться
    if (showShareOptions) {
        MyStyledDialog(onDismissRequest = { showShareOptions = false }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Поделиться изображением", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Поделиться оригиналом
                        val shareUri = actualUri // Используем actualUri напрямую
                        if (shareUri != null) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, shareUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Поделиться изображением"
                                )
                            )
                        } else {
                            Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
                        }
                        showShareOptions = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Поделиться оригиналом")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        // Показать диалог выбора мема
                        showMemeSelection = true
                        showShareOptions = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Зашифровать в мем")
                }
            }
        }
    }

// Диалог выбора мема
    if (showMemeSelection) {
        MemeSelectionDialog(
            onMemeSelected = { memeResId ->
                isProcessing = true
                viewModel.shareImageWithHiddenOriginal(
                    originalImageFile = actualImageFile,
                    memeResId = memeResId,
                    onResult = { uri ->
                        isProcessing = false
                        if (uri != null) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Поделиться изображением"
                                )
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Не удалось создать изображение",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                showMemeSelection = false
            },
            onDismiss = {
                showMemeSelection = false
            }
        )
    }

// Диалог загрузки
    if (isProcessing) {
        MyStyledDialog(onDismissRequest = {}) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomCircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Обработка...")
            }
        }
    }
}
