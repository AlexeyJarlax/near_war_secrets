package com.pavlov.MyShadowGallery.ui.images

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomCircularProgressIndicator
import com.pavlov.MyShadowGallery.theme.uiComponents.MyStyledDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pavlov.MyShadowGallery.theme.My3
import com.pavlov.MyShadowGallery.theme.My7
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomButtonOne
import com.pavlov.MyShadowGallery.theme.uiComponents.MyStyledDialogWithTitle
import com.pavlov.MyShadowGallery.ui.images.loaded.MemeSelectionDialog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

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
    val actualUri = viewModel.getFileUri(actualImageFile.name)

    if (actualUri == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
        return
    }

    var showShareOptions by remember { mutableStateOf(false) }
    var showMemeSelection by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var hiddenImageUri by remember { mutableStateOf<Uri?>(null) }

    val encryptionProgress by viewModel.encryptionProgress.collectAsState()

    // Создаём канал для очереди фраз
    val progressQueue = remember { Channel<String>(Channel.UNLIMITED) }

    // Список для отображения фраз
    val displayProgress = remember { mutableStateListOf<String>() }

    // Запускаем обработчик очереди
    LaunchedEffect(Unit) {
        for (step in progressQueue) {
            if (!displayProgress.contains(step)) { // Проверка на наличие фразы в списке
                displayProgress.add(step)
            }
            delay(1000L) // Задержка 1 секунда между фразами
        }
    }

    // Отправляем новые фразы в очередь
    LaunchedEffect(encryptionProgress) {
        encryptionProgress.forEach { step ->
            progressQueue.send(step)
        }
    }

    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Добавляем внутренние отступы
        ) {
            Text(text = name, style = MaterialTheme.typography.h6)
            Text(text = date, style = MaterialTheme.typography.subtitle2, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            ZoomableImage(
                uri = if (hiddenImageUri != null) hiddenImageUri else actualUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isItNew) { // кейс с 4 кнопками
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CustomButtonOne(
                            onClick = {
                                onSave?.invoke()
                                onDismiss()
                            },
                            text = "Сохранить",
                            textColor = My7,
                            iconColor = My7,
                            icon = Icons.Default.Save
                        )
                        CustomButtonOne(
                            onClick = { showShareOptions = true },
                            text = "Отправить",
                            textColor = My7,
                            iconColor = My7,
                            icon = Icons.Default.Share
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CustomButtonOne(
                            onClick = onDelete,
                            text = "Удалить",
                            textColor = My7,
                            iconColor = My7,
                            icon = Icons.Default.Delete
                        )

                        CustomButtonOne(
                            onClick = onDismiss,
                            text = "Закрыть",
                            textColor = My7,
                            iconColor = My7,
                            icon = Icons.Default.Close
                        )
                    }
                } else { // кейс с 3 кнопками
                    CustomButtonOne(
                        onClick = { showShareOptions = true },
                        text = "Поделиться",
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Share
                    )
                    CustomButtonOne(
                        onClick = onDelete,
                        text = "Удалить",
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Delete
                    )
                    CustomButtonOne(
                        onClick = onDismiss,
                        text = "Закрыть",
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Close
                    )
                }
            }
        }
    }

    // Диалог выбора способа поделиться
    if (showShareOptions) {
        MyStyledDialogWithTitle(
            onDismissRequest = { showShareOptions = false },
            title = {
                Text(
                    text = "Способ отправки",
                    style = MaterialTheme.typography.h6,
                )
            },
            gap = 0,
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // Добавляем внутренние отступы
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomButtonOne(
                        onClick = {
                            val shareUri = if (hiddenImageUri != null) hiddenImageUri else actualUri
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
                        text = "Поделиться оригиналом",
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.InsertPhoto
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CustomButtonOne( // поделиться шифровкой в мемчик
                        onClick = {
                            showMemeSelection = true
                            showShareOptions = false
                        },
                        text = "Зашифровать в мемчик",
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.HideImage
                    )
                }
            }
        )
    }

    if (showMemeSelection) { // Диалог выбора мема
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
                            onDismiss()
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

    if (isProcessing) { // Диалог загрузки с описанием процесса шифрования

        MyStyledDialog(onDismissRequest = {}) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Transparent),
            ) {
                Text(text = "Обработка шифрования", color = My3)
                Spacer(modifier = Modifier.height(8.dp))

                // Список фраз с прокруткой
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
                    displayProgress.forEach { step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share, // Изменена иконка на более подходящую
                                contentDescription = null,
                                tint = My3,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = step, color = My3)
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    CustomCircularProgressIndicator()
                }
            }
        }
    }
}
