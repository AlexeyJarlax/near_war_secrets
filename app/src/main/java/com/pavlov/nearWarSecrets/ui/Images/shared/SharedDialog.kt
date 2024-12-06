package com.pavlov.nearWarSecrets.ui.Images.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialog
import com.pavlov.nearWarSecrets.ui.Images.ZoomableImage
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.pavlov.nearWarSecrets.util.ToastExt
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.ui.Images.ImagesViewModel

@Composable
fun SharedDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    viewModel: ImagesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }

    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Новое изображение",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(16.dp))
            ZoomableImage(
                uri = uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        // Сохранение изображения
                        val success = viewModel.saveExtractedImage(uri, "ExtractedImages")
                        if (success) {
                            ToastExt.show("Изображение сохранено")
                            isSaved = true
                            onDismiss()
                        } else {
                            ToastExt.show("Ошибка при сохранении")
                        }
                    },
                    enabled = !isSaved
                ) {
                    Text("Сохранить")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        // Удаление изображения
                        viewModel.removeExtractedImage(uri)
                        ToastExt.show("Изображение удалено")
                        onDismiss()
                    }
                ) {
                    Text("Удалить")
                }
            }
        }
    }
}
