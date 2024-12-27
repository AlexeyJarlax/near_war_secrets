package com.pavlov.MyShadowGallery.ui.images.loaded

import com.pavlov.MyShadowGallery.R
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
import androidx.compose.ui.res.stringResource
import com.pavlov.MyShadowGallery.theme.My5
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomButtonOne
import com.pavlov.MyShadowGallery.theme.uiComponents.MyStyledDialogWithTitle

@Composable
fun MemeSelectionDialog(
    onMemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {

    val memeList = listOf(
        Pair(R.drawable.mem533x451_, R.drawable.mem533x451),
        Pair(R.drawable.mem683x749_, R.drawable.mem683x749),
        Pair(R.drawable.mem984x720_, R.drawable.mem984x720),
        Pair(R.drawable.mem1164x800_, R.drawable.mem1164x800),
        Pair(R.drawable.mem1920x1065_, R.drawable.mem1920x1065),
        Pair(R.drawable.mem1920x1277_, R.drawable.mem1920x1277),
        Pair(R.drawable.mem4621x2599_, R.drawable.mem4621x2599)
    )

    fun getSizeFromResourceName(resourceName: String): String {
        val sizeRegex = Regex("(\\d+x\\d+)")
        return sizeRegex.find(resourceName)?.value ?: "Unknown"
    }

    MyStyledDialogWithTitle(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.chuse_mem),
                style = MaterialTheme.typography.h6,
            )
        },
        gap = 0,
        content = {
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = stringResource(id = R.string.mem_info),
                color = My5
            )
            Spacer(modifier = Modifier.width(6.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(memeList) { (thumbnailResId, originalResId) ->
                    val resourceName = LocalContext.current.resources.getResourceEntryName(originalResId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onMemeSelected(originalResId) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = painterResource(id = thumbnailResId),
                            contentDescription = "Meme Thumbnail",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = getSizeFromResourceName(resourceName),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }
    )

    CustomButtonOne(
        onClick = onDismiss,
        text = stringResource(id = R.string.cancel),
        icon = Icons.Default.Cancel
    )
}