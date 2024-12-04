package com.pavlov.nearWarSecrets.ui.Images

import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomCircularProgressIndicator
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialog
import com.pavlov.nearWarSecrets.ui.Images.loaded.MemeSelectionDialog

// Диалог Извлеченных Изображений
@Composable
fun ExtractedImagesDialog(
    extractedImages: List<Uri>,
    onDismiss: () -> Unit
) {
    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp) // Ограничение максимальной высоты диалога
        ) {
            Text(
                text = "Извлеченные изображения",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Заполнение доступного пространства
            ) {
                items(extractedImages) { uri ->
                    ZoomableImage(
                        uri = uri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Закрыть")
            }
        }
    }
}

// Диалог Выбора Мемов
@Composable
fun MemeSelectionDialog(
    onMemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val memes = listOf(
        R.drawable.mem1,
        R.drawable.mem2,
        R.drawable.mem3
    )

    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "Выберите мем", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items = memes) { memeResId ->
                    Image(
                        painter = painterResource(id = memeResId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                onMemeSelected(memeResId)
                                onDismiss()
                            }
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Отмена")
            }
        }
    }
}

// Диалог Изображения с Опциями Поделиться и Удалить
@Composable
fun ImageDialog(
    fileName: String,
    viewModel: ImagesViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val imageFile = File(context.filesDir, fileName)
    val painter = rememberImagePainter(data = imageFile)
    val date = viewModel.getPhotoDate(fileName)
    val name = viewModel.getFileNameWithoutExtension(fileName)

    var showShareOptions by remember { mutableStateOf(false) }
    var showMemeSelection by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Основной диалог с изображением
    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = name, style = MaterialTheme.typography.h6)
            Text(text = date, style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
        }
    }

    // Диалог выбора способа поделиться
    if (showShareOptions) {
        MyStyledDialog(onDismissRequest = { showShareOptions = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Поделиться изображением", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Поделиться оригиналом
                        val uri = viewModel.getFileUri(fileName)
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
                    originalImageFile = imageFile,
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
        MyStyledDialog(onDismissRequest = { /* Нельзя закрыть диалог */ }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomCircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Обработка...", style = MaterialTheme.typography.body1)
            }
        }
    }
}

// Дополнительные Диалоги (Примеры)

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Подтвердить",
    dismissButtonText: String = "Отмена",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            }
        }
    }
}

@Composable
fun MessageDialog(
    title: String,
    message: String,
    buttonText: String = "Закрыть",
    onButtonClick: () -> Unit
) {
    MyStyledDialog(onDismissRequest = onButtonClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onButtonClick) {
                Text(buttonText)
            }
        }
    }
}