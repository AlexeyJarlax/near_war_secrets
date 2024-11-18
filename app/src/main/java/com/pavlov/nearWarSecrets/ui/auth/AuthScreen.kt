package com.pavlov.nearWarSecrets.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToMain: () -> Unit,
    onNavigateToTwoStepsForSave: () -> Unit
) {
    val isPasswordExist by viewModel.isPasswordExist.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()
    val counter by viewModel.counter.collectAsState()
    val navigateToMain by viewModel.navigateToMain.collectAsState()
    val navigateToTwoStepsForSave by viewModel.navigateToTwoStepsForSave.collectAsState()

    LaunchedEffect(navigateToMain) {
        if (navigateToMain) {
            onNavigateToMain()
        }
    }

    LaunchedEffect(navigateToTwoStepsForSave) {
        if (navigateToTwoStepsForSave) {
            onNavigateToTwoStepsForSave()
        }
    }

    if (loading) {
        // Show loading indicator
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        // Show password input
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Введите пароль")
            Spacer(modifier = Modifier.height(8.dp))
            var password by remember { mutableStateOf("") }
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.onPasswordEntered(password) }) {
                Text("Войти")
            }
            if (message.isNotEmpty()) {
                Text(text = message, color = MaterialTheme.colors.error)
            }
            Text(text = "Осталось попыток: $counter")
        }
    }
}