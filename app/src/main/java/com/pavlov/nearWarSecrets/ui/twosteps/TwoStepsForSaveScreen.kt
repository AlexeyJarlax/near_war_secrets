package com.pavlov.nearWarSecrets.ui.twosteps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.R

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

    LaunchedEffect(navigateToSetPassword) {
        if (navigateToSetPassword) {
            onNavigateToSetPassword()
        }
    }

    LaunchedEffect(navigateToMain) {
        if (navigateToMain) {
            onNavigateToMain()
        }
    }

    LaunchedEffect(navigateToKeyInput) {
        if (navigateToKeyInput) {
            onNavigateToKeyInput()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            0 -> {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null
                )
                Text("Шаг 0: Начало")
                Button(onClick = { viewModel.onInputButtonClicked() }) {
                    Text("Продолжить")
                }
            }
            1 -> {
                // Здесь можно добавить анимацию или переходы, если нужно
                Text("Установить пароль?")
                Row {
                    Button(onClick = { viewModel.onYesClicked() }) {
                        Text("Да")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.onNoClicked() }) {
                        Text("Нет")
                    }
                }
            }
            3 -> {
                Text("Установить ключ шифрования?")
                Row {
                    Button(onClick = { viewModel.onYesClicked() }) {
                        Text("Да")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.onNoClicked() }) {
                        Text("Нет")
                    }
                }
            }
        }
    }
}