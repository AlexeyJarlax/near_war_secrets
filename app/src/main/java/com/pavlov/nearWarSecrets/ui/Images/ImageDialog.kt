package com.pavlov.nearWarSecrets.ui.Images

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomCircularProgressIndicator
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialog
import com.pavlov.nearWarSecrets.ui.Images.loaded.MemeSelectionDialog

@Composable
fun ImageDialog(
    uri: Uri,
    viewModel: ImagesViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    showSaveButton: Boolean = false // Новый параметр для отображения кнопки "Сохранить"
) {
    val context = LocalContext.current
    val imageFile = File(uri.path ?: "")

    if (false) {
        // Если файл не найден, показываем сообщение об ошибке и закрываем диалог
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Файл не найден: ${uri.path}", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
        return
    }

    // Определяем директорию файла
    val photoListDir = File(context.filesDir, "PhotoList")
    val extractedImagesDir = File(context.filesDir, "ExtractedImages")

    // Проверяем, в какой директории находится файл
    val actualImageFile = when {
        File(photoListDir, imageFile.name).exists() -> File(photoListDir, imageFile.name)
        File(extractedImagesDir, imageFile.name).exists() -> File(extractedImagesDir, imageFile.name)
        else -> imageFile // Если файл не найден в ожидаемых директориях, используем переданный файл
    }

    // Обновляем Uri на основе найденного файла
    val actualUri = Uri.fromFile(actualImageFile)

    val painter = rememberImagePainter(data = actualImageFile)
    val date = viewModel.getPhotoDate(actualImageFile.name)
    val name = viewModel.getFileNameWithoutExtension(actualImageFile.name)

    var showShareOptions by remember { mutableStateOf(false) }
    var showMemeSelection by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Основной диалог с изображением
    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = name, style = MaterialTheme.typography.h6)
            Text(text = date, style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.height(8.dp))
            // Отображение изображения с возможностью масштабирования
            ZoomableImage(
                uri = actualUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Первая строка кнопок: Поделиться и Удалить
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    showShareOptions = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Поделиться"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Вторая строка кнопок: Сохранить (условно) и Закрыть
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showSaveButton) {
                    IconButton(onClick = {
                        // Вызов метода сохранения из ViewModel
                        val success = viewModel.saveExtractedImage(actualUri)
                        if (success) {
                            Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Сохранить"
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть"
                    )
                }
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
                        val shareUri = viewModel.getFileUri(actualImageFile.name)
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