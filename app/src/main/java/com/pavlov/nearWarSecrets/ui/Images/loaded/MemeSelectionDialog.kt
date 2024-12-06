package com.pavlov.nearWarSecrets.ui.Images.loaded

import com.pavlov.nearWarSecrets.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialog
import androidx.compose.foundation.shape.RoundedCornerShape

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
            modifier = Modifier.padding(16.dp)
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
            Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Отмена")
            }
        }
    }
}