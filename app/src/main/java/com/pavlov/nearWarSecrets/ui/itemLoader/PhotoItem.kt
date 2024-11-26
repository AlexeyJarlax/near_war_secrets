package com.pavlov.nearWarSecrets.ui.itemLoader

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import java.io.File

@Composable
fun PhotoItem(
    fileName: String,
    viewModel: ItemLoaderViewModel,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    val imageFile = File(context.filesDir, fileName)
    val date = viewModel.getPhotoDate(fileName)
    val name = viewModel.getFileNameWithoutExtension(fileName)
    val encryptionKeyName = viewModel.getEncryptionKeyName(fileName)
    val painter = rememberImagePainter(data = imageFile)

    Column(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onImageClick(fileName) }
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = date,
            style = MaterialTheme.typography.caption
        )
        if (encryptionKeyName.isNotEmpty()) {
            Text(
                text = encryptionKeyName,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary
            )
        }
    }
}