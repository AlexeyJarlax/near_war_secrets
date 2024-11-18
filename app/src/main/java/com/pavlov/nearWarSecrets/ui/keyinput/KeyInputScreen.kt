package com.pavlov.nearWarSecrets.ui.keyinput

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.ui.main.MainActivity
import com.pavlov.nearWarSecrets.util.APKM

@Composable
fun KeyInputScreen(
    viewModel: KeyInputViewModel = hiltViewModel(),
    onNavigateToMain: () -> Unit
) {
    val keyName by viewModel.keyName.collectAsState()
    val keyValue by viewModel.keyValue.collectAsState()
    val message by viewModel.message.collectAsState()
    val isKeyValid by viewModel.isKeyValid.collectAsState()
    val navigateToMain by viewModel.navigateToMain.collectAsState()
    val showNewKeyFields by viewModel.showNewKeyFields.collectAsState()
    val showOldKeys by viewModel.showOldKeys.collectAsState()
    val oldKeys by viewModel.oldKeys.collectAsState()
    val context = LocalContext.current as MainActivity

    LaunchedEffect(navigateToMain) {
        if (navigateToMain) {
            onNavigateToMain()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (showNewKeyFields) {
            TextField(
                value = keyName,
                onValueChange = { viewModel.keyName.value = it },
                label = { Text("Имя ключа") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = keyValue,
                onValueChange = { viewModel.onKeyValueChange(it) },
                label = { Text("Ключ") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                color = if (isKeyValid) Color.Green else Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.generateRandomKey() }) {
                Text("Сгенерировать ключ")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveKey() },
                enabled = isKeyValid
            ) {
                Text("Сохранить ключ")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.onDoNotUseKey() }) {
                Text("Не использовать ключ")
            }
        }

        if (showOldKeys) {
            Text("Доступные ключи:")
            oldKeys.forEach { (keyName, keyNumber) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = keyName)
                    Row {
                        Button(onClick = { viewModel.selectOldKey(keyNumber) }) {
                            Text("Выбрать")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.deleteKey(keyNumber) }) {
                            Text("Удалить")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                viewModel.keyName.value = APKM(context).generateUniqueKeyName()
                viewModel._showNewKeyFields.value = true
                viewModel._showOldKeys.value = false
            }) {
                Text("Добавить новый ключ")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.onDoNotUseKey() }) {
                Text("Не использовать ключ")
            }
        }

        if (message.isNotEmpty()) {
            Text(text = message, color = MaterialTheme.colors.error)
        }
    }
}