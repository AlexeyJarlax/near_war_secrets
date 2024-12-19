package com.pavlov.MyShadowGallery.ui.images.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.pavlov.MyShadowGallery.ui.images.ImagesViewModel
import com.pavlov.MyShadowGallery.util.APK.RECEIVED_FROM_OUTSIDE
import java.io.File

@Composable
fun SharedItem(
    fileName: String,
    viewModel: ImagesViewModel,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    val imageFile = File(context.filesDir, "$RECEIVED_FROM_OUTSIDE/$fileName")
    val date = viewModel.getPhotoDate(fileName)
    val name = viewModel.getFileNameWithoutExtension(fileName)
    val painter = rememberAsyncImagePainter(model = imageFile)

    Column(
        modifier = Modifier
            .padding(8.dp, top = 16.dp)
            .clickable { onImageClick(fileName) }
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painter,
                contentDescription = "Shared Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.subtitle2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = date,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
