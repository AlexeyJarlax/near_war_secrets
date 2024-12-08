package com.pavlov.nearWarSecrets.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.theme.uiComponents.ConfirmationDialog
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.theme.My7

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

                    // Кнопка "Очистить хранилище" с подтверждением
                    CustomButtonOne(
                        onClick = { showClearStorageDialog = true },
                        text = stringResource(R.string.clearing_the_storage),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.FolderOff
                    )

                    // Кнопка "Сбросить настройки" с подтверждением
                    CustomButtonOne(
                        onClick = { showResetSettingsDialog = true },
                        text = stringResource(R.string.reset_settings),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.SettingsBackupRestore,
                    )

                    // Кнопка "Выбрать язык"
                    CustomButtonOne(
                        onClick = { showLanguageDialog = true },
                        text = stringResource(R.string.language),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Language,
                    )

                    // Кнопка "О приложении"
//                    CustomButtonOne(
//                        onClick = onAboutClicked,
//                        text = stringResource(R.string.about_the_app),
//                    textColor = My7,
//                    iconColor = My7,
//                        icon = Icons.Default.AppRegistration,
//                    )

                    // Кнопка "Личные данные"
                    CustomButtonOne(
                        onClick = { viewModel.togglePersonalData() },
                        textColor = My7,
                        iconColor = My7,
                        text = personalDataText,
                        icon = Icons.Default.PersonOff,
                    )

                    // Кнопка "Настройки безопасности"
                    CustomButtonOne(
                        onClick = onSecuritySettingsClicked,
                        text = stringResource(R.string.security_settings),
                        textColor = My7,
                        iconColor = My7,
                        icon = Icons.Default.Shield,
                    )
                }

                // Диалог выбора языка
                if (showLanguageDialog) {
                    LanguageSelectionDialog(
                        onDismiss = { showLanguageDialog = false },
                        onLanguageSelected = { selectedLanguage ->
                            viewModel.setLanguage(selectedLanguage)
                        }
                    )
                }

                // Диалог подтверждения для очистки хранилища
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

                // Диалог подтверждения для сброса настроек
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
