package com.pavlov.nearWarSecrets.ui.setpassword

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground

@Composable
fun SetPasswordScreen(
    viewModel: SetPasswordViewModel = hiltViewModel(),
    onPasswordSet: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
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
                    if (viewModel.isPasswordExist) {
//                        TextField(
//                            value = oldPassword,
//                            onValueChange = { oldPassword = it },
//                            label = { Text("Старый пароль") }
//                        )
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Старый пароль") },
                            placeholder = { Text("qwerty, 123456, 111 не самые лучшие примеры пароля") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
//                    TextField(
//                        value = newPassword,
//                        onValueChange = { newPassword = it },
//                        label = { Text("Новый пароль") }
//                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Новый пароль") },
                        placeholder = { Text("qwerty, 123456, 111 не самые лучшие примеры пароля") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
//                    TextField(
//                        value = confirmPassword,
//                        onValueChange = { confirmPassword = it },
//                        label = { Text("Подтвердите пароль") }
//                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Подтвердите пароль") },
                        placeholder = { Text("Должен совпадать в обоих полях") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (newPassword == confirmPassword) {
                            viewModel.savePassword(newPassword)
                            onPasswordSet()
                        } else {
                            message = "Пароли не совпадают"
                        }
                    }) {
                        Text("Сохранить пароль")
                    }
                    if (message.isNotEmpty()) {
                        Text(text = message, color = MaterialTheme.colors.error)
                    }
                }
            }
        }
    )
}