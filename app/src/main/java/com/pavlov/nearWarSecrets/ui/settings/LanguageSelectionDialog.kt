package com.pavlov.nearWarSecrets.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.theme.My7
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialog
import com.pavlov.nearWarSecrets.theme.uiComponents.MyStyledDialogWithTitle

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit, onLanguageSelected: (String) -> Unit) {
    val languageOptions = listOf("Русский", "English", "汉语", "Español")
    val languageCodes = listOf("ru", "en", "zh", "es")

    MyStyledDialogWithTitle(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выберите язык",
                style = MaterialTheme.typography.h6,
            )
        },
        content = {
            Column {
                languageOptions.forEachIndexed { index, language ->
                    TextButton(onClick = {
                        onLanguageSelected(languageCodes[index])
                        onDismiss()
                    }) {
                        Text(text = language, style = MaterialTheme.typography.body1)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}