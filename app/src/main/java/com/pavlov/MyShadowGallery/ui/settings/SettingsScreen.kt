package com.pavlov.MyShadowGallery.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.theme.uiComponents.ConfirmationDialog
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomButtonOne
import com.pavlov.MyShadowGallery.theme.uiComponents.MatrixBackground
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.MyShadowGallery.theme.My7

@Composable
fun SettingsScreen(
    navController: NavController,
    onAboutClicked: () -> Unit,
    onSecuritySettingsClicked: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val personalDataText by viewModel.personalDataText.collectAsState()
    val language by viewModel.language.collectAsState()

    // Состояния для диалогов подтверждения
    var showClearStorageDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MatrixBackground()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // Кнопка "Выбрать язык"
//                    CustomButtonOne(
//                        onClick = { showLanguageDialog = true },
//                        text = stringResource(R.string.language),
//                        textColor = My7,
//                        iconColor = My7,
//                        icon = Icons.Default.Language,
//                    )

                    // Кнопка "Настройки безопасности"
                    CustomButtonOne(
                        onClick = onSecuritySettingsClicked,
                        text = stringResource(R.string.security_settings),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Shield,
                    )

                    // Кнопка "Очистить хранилище"
                    CustomButtonOne(
                        onClick = { showClearStorageDialog = true },
                        text = stringResource(R.string.clearing_the_storage),
                        textColor = Color.Red,
                        iconColor = Color.Red,
                        icon = Icons.Default.FolderOff
                    )

                    // Кнопка "Сбросить настройки"
                    CustomButtonOne(
                        onClick = { showResetSettingsDialog = true },
                        text = stringResource(R.string.reset_settings),
                        textColor = Color.Red,
                        iconColor = Color.Red,
                        icon = Icons.Default.SettingsBackupRestore,
                    )
                }

                if (showLanguageDialog) {
                    LanguageSelectionDialog(
                        onDismiss = { showLanguageDialog = false },
                        onLanguageSelected = { selectedLanguage ->
                            viewModel.setLanguage(selectedLanguage)
                        }
                    )
                }

                if (showClearStorageDialog) {
                    ConfirmationDialog(
                        title = stringResource(R.string.confirm),
                        message = stringResource(R.string.clear_storage),
                        onConfirm = {
                            viewModel.clearStorage()
                            showClearStorageDialog = false
                        },
                        onDismiss = { showClearStorageDialog = false }
                    )
                }

                if (showResetSettingsDialog) {
                    ConfirmationDialog(
                        title = stringResource(R.string.confirm),
                        message = stringResource(R.string.reset_settings_confirm),
                        onConfirm = {
                            viewModel.resetSettings()
                            showResetSettingsDialog = false
                        },
                        onDismiss = { showResetSettingsDialog = false }
                    )
                }
            }
        }
    )
}
