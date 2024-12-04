package com.pavlov.nearWarSecrets.ui.Images.loaded

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.pavlov.nearWarSecrets.ui.Images.ImagesViewModel
import java.io.File

@Composable
fun PhotoItem(
    fileName: String,
    viewModel: ImagesViewModel,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    val imageFile = File(context.filesDir, fileName)
    val date = viewModel.getPhotoDate(fileName)
    val name = viewModel.getFileNameWithoutExtension(fileName)
    val painter = rememberImagePainter(data = imageFile)

    Column(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onImageClick(fileName) }
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = date,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}