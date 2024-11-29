package com.pavlov.nearWarSecrets.ui.auth

import com.pavlov.nearWarSecrets.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.theme.My4
import com.pavlov.nearWarSecrets.theme.My5
import com.pavlov.nearWarSecrets.theme.My7
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
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
                            color = My4
                        )
                        val randomSymbol = symbols.random()
                        Text(
                            text = randomSymbol.toString(),
                            color = My4,
                            style = MaterialTheme.typography.h4,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(34.dp),
                        verticalArrangement = Arrangement.Center
                    ) {

                        var password by remember { mutableStateOf("") }

                        Text(
                            text = if (password.isEmpty()) "Введите пароль" else ""
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CustomOutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Пароль",
                            placeholder = "Введите пароль",
                            backgroundColor = My4
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CustomButtonOne(
                            onClick = { viewModel.onPasswordEntered(password) },
                            text = stringResource(R.string.enter),
                            textColor = if (password.isEmpty()) My5 else My7,
                            iconColor = if (password.isEmpty()) My5 else My7,
                            iconResId = R.drawable.login_30dp
                        )

                        if (message.isNotEmpty()) {
                            Text(text = message, color = MaterialTheme.colors.error)
                        }
                        if (hasError) {
                            Text(
                                text = "Осталось попыток: $counter",
                                color = MaterialTheme.colors.error
                            )
                        }
                    }
                }
            }
        }
    )
}