package com.pavlov.nearWarSecrets.ui.auth

import android.R.attr.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.theme.MyTheme
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomOutlinedTextField
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixAnimationSettings.symbols
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToItemLoader: () -> Unit,
    onNavigateToTwoStepsForSave: () -> Unit
) {
    val isPasswordExist by viewModel.isPasswordExist.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()
    val counter by viewModel.counter.collectAsState()
    val navigateToItemLoader by viewModel.navigateToItemLoader.collectAsState()
    val navigateToTwoStepsForSave by viewModel.navigateToTwoStepsForSave.collectAsState()
    val hasError by viewModel.hasError.collectAsState()

    LaunchedEffect(navigateToItemLoader) {
        if (navigateToItemLoader) onNavigateToItemLoader()
    }

    LaunchedEffect(navigateToTwoStepsForSave) {
        if (navigateToTwoStepsForSave) onNavigateToTwoStepsForSave()
    }

    Scaffold(
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MatrixBackground()
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            color = Color(0xFF87E01A)
                        )
                        val randomSymbol = symbols.random()
                        Text(
                            text = randomSymbol.toString(),
                            color = Color(0xFF87E01A),
                            style = MaterialTheme.typography.h4,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Введите пароль",
                            modifier = Modifier
                                .padding(start = 14.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        var password by remember { mutableStateOf("") }
                        CustomOutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Пароль",
                            placeholder = "Введите пароль",
                            backgroundColor = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.onPasswordEntered(password) },
                            modifier = Modifier.padding(start = 14.dp)
                        ) {
                            Text("Войти")
                        }
                        if (message.isNotEmpty()) {
                            Text(text = message, color = MaterialTheme.colors.error)
                        }
                        if (hasError) {
                            Text(
                                text = "Осталось попыток: $counter",
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}