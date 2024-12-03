package com.pavlov.nearWarSecrets.ui.itemLoader

import com.pavlov.nearWarSecrets.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color

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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier
                .border(width = 2.dp, color = Color.Green, shape = RoundedCornerShape(8.dp))
        ) {
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
}
