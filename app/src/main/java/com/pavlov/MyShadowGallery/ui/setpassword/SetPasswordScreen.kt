package com.pavlov.MyShadowGallery.ui.setpassword

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SetPasswordScreen(
    viewModel: SetPasswordViewModel = hiltViewModel(),
    onPasswordSet: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (viewModel.isPasswordExist) {
            TextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Старый пароль") }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Новый пароль") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Подтвердите пароль") }
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