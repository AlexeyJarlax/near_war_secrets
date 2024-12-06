package com.pavlov.nearWarSecrets.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit, onLanguageSelected: (String) -> Unit) {
    val languageOptions = listOf("Русский", "English", "汉语", "Español")
    val languageCodes = listOf("ru", "en", "zh", "es")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        buttons = {
            Column {
                languageOptions.forEachIndexed { index, language ->
                    TextButton(onClick = {
                        onLanguageSelected(languageCodes[index])
                        onDismiss()
                    }) {
                        Text(text = language)
                    }
                }
            }
        }
    )
}