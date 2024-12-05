package com.pavlov.nearWarSecrets.ui.Images.extracted

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import com.pavlov.nearWarSecrets.ui.Images.ImagesViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavedImageItem(
    uri: Uri,
    viewModel: ImagesViewModel,
    onImageClick: (Uri) -> Unit
) {
    val context = LocalContext.current
    val file = remember(uri) { File(uri.path ?: "") }
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(file.lastModified()) { dateFormatter.format(Date(file.lastModified())) }
    val fileName = remember(file.name) { file.nameWithoutExtension }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onImageClick(uri) }
    ) {
        Image(
            painter = rememberImagePainter(data = uri),
            contentDescription = "Saved Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = fileName,
            style = MaterialTheme.typography.subtitle1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}