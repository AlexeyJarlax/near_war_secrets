package com.pavlov.MyShadowGallery.theme.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pavlov.MyShadowGallery.theme.My7

@Composable
fun MyStyledDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = Color.Green,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun MyStyledDialogWithTitle(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    gap: Int = 10
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = gap.dp)
                    .border(
                        width = 2.dp,
                        color = Color.Green,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    content()
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colors.surface)
                    .padding(horizontal = 8.dp)

            ) {
                title()
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Подтвердить",
    dismissButtonText: String = "Отменить",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    MyStyledDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(title)

            Spacer(modifier = Modifier.height(8.dp))

            Text(message, style = MaterialTheme.typography.body1)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onDismiss) {
                    Text(
                        text = dismissButtonText,
                        color = My7
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onConfirm) {
                    Text(
                        text = confirmButtonText,
                        color = My7)
                }
            }
        }
    }
}

//@Composable
//fun OptionDialog(
//    onDismiss: () -> Unit,
//    onSelected: (String) -> Unit
//    title: String
//) {
//    val options = listOf()
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { title },
//        buttons = {
//            Column {
//                options.forEachIndexed { index, item ->
//                    TextButton(onClick = {
//                        onSelected()
//                        onDismiss()
//                    }) {
//                        androidx.compose.material3.Text(text = item)
//                    }
//                }
//            }
//        }
//    )
//}

@Composable
fun MessageDialog(
    title: String,
    message: String,
    buttonText: String = "Закрыть",
    onButtonClick: () -> Unit
) {
    MyStyledDialog(onDismissRequest = onButtonClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onButtonClick) {
                Text(buttonText)
            }
        }
    }
}