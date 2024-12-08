package com.pavlov.nearWarSecrets.ui.Images.loaded

import android.content.Context
import android.graphics.BitmapFactory
import com.pavlov.nearWarSecrets.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialog
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialogWithTitle
import timber.log.Timber

@Composable
fun MemeSelectionDialog(
    onMemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val memeList = listOf(
        R.drawable.mem3,
        R.drawable.mem2,
        R.drawable.mem1,
        R.drawable.x533x451
    )

    // Получение контекста для доступа к ресурсам
    val context = LocalContext.current

    // Функция для получения размера изображения
    fun getImageSize(resourceId: Int): String {
        return try {
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            "${bitmap.width}x${bitmap.height}"
        } catch (e: Exception) {
            Timber.tag("MemeSelectionDialog").e(e, "Ошибка при получении размера изображения")
            "Unknown"
        }
    }

    MyStyledDialogWithTitle(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выберите мемчик",
                style = MaterialTheme.typography.h6,
            )
        },
        gap = 0,
        content = {
            Spacer(modifier = Modifier.width(16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Ограничение по высоте для прокрутки
            ) {

                items(memeList) { memeResId ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onMemeSelected(memeResId) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = memeResId),
                            contentDescription = "Meme",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = getImageSize(memeResId),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }
    )

    CustomButtonOne(
        onClick = onDismiss,
        text = "Отмена",
        icon = Icons.Default.Cancel
    )
}
