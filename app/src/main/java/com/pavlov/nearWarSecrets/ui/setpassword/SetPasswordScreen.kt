package com.pavlov.nearWarSecrets.ui.setpassword

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.R
import com.pavlov.nearWarSecrets.theme.My4
import com.pavlov.nearWarSecrets.theme.My5
import com.pavlov.nearWarSecrets.theme.My7
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomButtonOne
import com.pavlov.nearWarSecrets.theme.uiComponents.CustomOutlinedTextField
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground

@Composable
fun SetPasswordScreen(
    viewModel: SetPasswordViewModel = hiltViewModel(),
    onPasswordSet: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Scaffold(
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MatrixBackground()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {

                    CustomOutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "Новый пароль",
                        placeholder = "Введите новый пароль тут",
                        backgroundColor = My4,
                        isPassword = false,
                        keyboardActions = { ImeAction.Done }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CustomOutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Проверка ввода",
                        placeholder = "Подтвердите новый пароль тут",
                        backgroundColor = My4,
                        isPassword = false,
                        keyboardActions = {
                            if (newPassword == confirmPassword) {
                                viewModel.savePassword(newPassword)
                                onPasswordSet()
                            } else {
                                message = "Пароли не совпадают"
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    CustomButtonOne(
                        onClick = {
                            if (newPassword == confirmPassword) {
                                viewModel.savePassword(newPassword)
                                onPasswordSet()
                            } else {
                                message = "Пароли не совпадают"
                            }
                        },
                        text = stringResource(com.pavlov.nearWarSecrets.R.string.enter),
                        textColor = if (confirmPassword.isEmpty()) My5 else My7,
                        iconColor = if (confirmPassword.isEmpty()) My5 else My7,
                        icon = R.drawable.login_30dp
                    )

                    if (message.isNotEmpty()) {
                        Text(text = message, color = MaterialTheme.colors.error)
                    }
                }
            }
        }
    )
}
