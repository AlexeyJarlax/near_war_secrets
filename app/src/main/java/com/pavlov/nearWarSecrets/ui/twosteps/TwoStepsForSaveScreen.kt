package com.pavlov.nearWarSecrets.ui.twosteps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.theme.My5
import com.pavlov.nearWarSecrets.theme.My7
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground

@Composable
fun TwoStepsForSaveScreen(
    viewModel: TwoStepsForSaveViewModel = hiltViewModel(),
    onNavigateToSetPassword: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToKeyInput: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val navigateToSetPassword by viewModel.navigateToSetPassword.collectAsState()
    val navigateToMain by viewModel.navigateToMain.collectAsState()
    val navigateToKeyInput by viewModel.navigateToKeyInput.collectAsState()

    LaunchedEffect(navigateToSetPassword) { //поставить пароль на вход
        if (navigateToSetPassword) {
            onNavigateToSetPassword()
        }
    }

    LaunchedEffect(navigateToMain) { //ITEM_LOADER
        if (navigateToMain) {
            onNavigateToMain()
        }
    }

    LaunchedEffect(navigateToKeyInput) { // ключ шифрования
        if (navigateToKeyInput) {
            onNavigateToKeyInput()
        }
    }

    Scaffold(
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MatrixBackground()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        0 -> {
                            Text("Два коротких шага, чтобы настроить безопасность приложения")
                            CustomButtonOne(
                                onClick = { viewModel.onNextButtonClicked() },
                                text = "Продолжить",
                                iconResId = R.drawable.login_30dp
                            )
                        }

                        1 -> {
                            Text("Установить пароль для входа в приложение?")
                            Row {
                                CustomButtonOne(
                                    onClick = { viewModel.onYesClicked() },
                                    text = "Да"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CustomButtonOne(
                                    onClick = { viewModel.onNoClicked() },
                                    text = "Нет"
                                )
                            }
                        }

                        3 -> {
                            Text("Установить ключ шифрования?")
                            Row {
                                CustomButtonOne(
                                    onClick = { viewModel.onYesClicked() },
                                    text = "Да"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CustomButtonOne(
                                    onClick = { viewModel.onNoClicked() },
                                    text = "Нет"
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}