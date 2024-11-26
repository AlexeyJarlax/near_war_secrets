package com.pavlov.nearWarSecrets.ui.theme.uiComponents

import com.pavlov.nearWarSecrets.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import com.pavlov.nearWarSecrets.data.model.Option

@Composable
fun CustomYesOrNoDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title, style = TextStyle(fontSize = 18.sp)) },
        text = { Text(text = text, style = TextStyle(fontSize = 18.sp)) },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.my_very_wight)
                )
            ) {
                Text("✔️", style = TextStyle(fontSize = 18.sp))
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.my_very_wight)
                )
            ) {
                Text("❌", style = TextStyle(fontSize = 18.sp))
            }
        },
        backgroundColor = colorResource(id = R.color.my_very_wight),
        contentColor = colorResource(id = R.color.my_black)
    )
}

/** пример реализации:
CustomYesOrNoDialog(
stringResource(id = R.string.exit_dialog),
"",
onDismiss,
onConfirm
)
 */

@Composable
fun CustomOptionDialog(
    title: String,
    options: List<Option>,
    onDismiss: () -> Unit,
    onOptionSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.my_very_wight))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.run {
                            fillMaxWidth()
                                .clickable {
                                    Timber.d("Option clicked: ${option.text} with id: ${option.id}")
                                    onOptionSelected(option.id)
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        }
                    ) {
                        Image(
                            painter = painterResource(id = option.iconRes),
                            contentDescription = option.text,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colorFilter = ColorFilter.tint(Color.DarkGray)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.body1.copy(fontSize = 16.sp),
                            color = colorResource(id = R.color.my_black)
                        )
                    }
                    Divider(color = Color.Gray, thickness = 0.5.dp)
                }
            }
        },
        confirmButton = {
            IconButton(
                onClick = { onDismiss() },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear),
                    contentDescription = "Закрыть",
                    tint = colorResource(id = R.color.my_black)
                )
            }
        },
        dismissButton = {},
        backgroundColor = colorResource(id = R.color.my_very_wight),
        contentColor = colorResource(id = R.color.my_black)
    )
}

/** пример реализации:
@Composable
fun FilterDialog(onOptionSelected: (Int) -> Unit, onDismiss: () -> Unit) {
val options = listOf(
Option(
id = 1,  // Уникальный идентификатор
iconRes = R.drawable.keep_public_100dp,  // Замените на ваш ресурс иконки
text = stringResource(id = R.string.show_events)
),
Option(
id = 2,
iconRes = R.drawable.user_online_30dp,
text = stringResource(id = R.string.show_users_online)
),
Option(
id = 3,
iconRes = R.drawable.user_any_30dp,
text = stringResource(id = R.string.show_all_users)
)
)

CustomOptionDialog(
title = stringResource(id = R.string.select_filter),
options = options,
onDismiss = onDismiss,
onOptionSelected = { selectedId ->
onOptionSelected(selectedId)  // Передаем id выбранной опции
}
)
}
 */

@Composable
fun CustomTextInputDialog(
    title: String,
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    maxLength: Int = 50,
    maxNewLines: Int = 1
) {
    var textState by remember { mutableStateOf(initialText) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            keyboardController?.hide()
        },
        title = { Text(text = title) },
        text = {
            Column {
                TextField(
                    value = textState,
                    onValueChange = {
                        textState = it
                        errorMessage = null
                    },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(textState)
                keyboardController?.hide()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                keyboardController?.hide()
            }) {
                Text("Cancel")
            }
        }
    )
}

/**
CustomTextInputDialog(
title = stringResource(id = R.string.edit_description),
initialText = if (user?.description.isNullOrEmpty()) {
stringResource(id = R.string.description_sample)
} else {
user?.description ?: ""
},
onDismiss = { showDescriptionDialog = false },
onConfirm = { newDescription ->
viewModel.updateUserDescription(newDescription) {
showDescriptionDialog = false
Timber.tag("ProfileScreen").d("=== Описание пользователя обновлено")
}
},
maxLength = 500, // Максимальная длина для описания
maxNewLines = 20 // Максимум 20 переносов строк
)
 */

//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun CustomCheckboxDialog(
//    title: String,
//    options: Array<String>,
//    selectedItems: MutableMap<String, Boolean>,
//    onDismiss: () -> Unit,
//    onConfirm: (Set<String>) -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = { onDismiss() },
//        title = {
//            Text(
//                text = title,
//                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
//            )
//        },
//        text = {
//            FlowRow(
//                mainAxisSize = SizeMode.Expand,
//                mainAxisSpacing = 16.dp,
//                crossAxisSpacing = 8.dp
//            ) {
//                options.forEach { option ->
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .padding(vertical = 6.dp)
//                    ) {
//                        CustomCheckbox(
//                            checked = selectedItems[option] ?: false,
//                            onCheckedChange = { isChecked ->
//                                selectedItems[option] = isChecked
//                            },
//                            enabled = true
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = option,
//                            style = TextStyle(fontSize = 16.sp),
//                            modifier = Modifier.wrapContentWidth()
//                        )
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    val selected = selectedItems.filter { it.value }.keys
//                    onConfirm(selected.toSet())
//                    onDismiss()
//                },
//                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.my_very_wight))
//            ) {
//                Text("✔️", style = TextStyle(fontSize = 18.sp))
//            }
//        },
//        dismissButton = {
//            Button(
//                onClick = onDismiss,
//                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.my_very_wight))
//            ) {
//                Text("❌", style = TextStyle(fontSize = 18.sp))
//            }
//        },
//        backgroundColor = colorResource(id = R.color.my_very_wight),
//        contentColor = colorResource(id = R.color.my_black)
//    )
//}