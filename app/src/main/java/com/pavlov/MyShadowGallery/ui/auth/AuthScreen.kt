package com.pavlov.MyShadowGallery.ui.auth

import com.pavlov.MyShadowGallery.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavlov.MyShadowGallery.theme.My4
import com.pavlov.MyShadowGallery.theme.My5
import com.pavlov.MyShadowGallery.theme.My7
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomButtonOne
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomOutlinedTextField
import com.pavlov.MyShadowGallery.theme.uiComponents.MatrixBackground
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomCircularProgressIndicator

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
                MatrixBackground(255)
                if (loading) {
                        CustomCircularProgressIndicator()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(34.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        var password by remember { mutableStateOf("") }

                        Text(
                            text = if (password.isEmpty()) stringResource(id = R.string.add_password) else ""
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        CustomOutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = stringResource(id = R.string.password),
                            placeholder = "...",
                            backgroundColor = My4,
                            isPassword = true,
                            keyboardActions =  {viewModel.onPasswordEntered(password)}
                            )

                        Spacer(modifier = Modifier.height(8.dp))

                        CustomButtonOne(
                            onClick = { viewModel.onPasswordEntered(password) },
                            text = stringResource(R.string.enter),
                            textColor = if (password.isEmpty()) My5 else My7,
                            iconColor = if (password.isEmpty()) My5 else My7,
                            fontSize = 20.sp,
                            iconPadding = 12,
                            icon = R.drawable.login_30dp
                        )

                        if (message.isNotEmpty()) {
                            Text(text = message, color = MaterialTheme.colors.error)
                        }
                        if (hasError) {
                            Text(
                                text = stringResource(id = R.string.attempts_left, counter),
                                color = MaterialTheme.colors.error
                            )
                        }
                    }
                }
            }
        }
    )
}
