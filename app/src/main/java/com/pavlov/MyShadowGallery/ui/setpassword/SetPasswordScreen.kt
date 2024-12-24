package com.pavlov.MyShadowGallery.ui.setpassword

import com.pavlov.MyShadowGallery.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pavlov.MyShadowGallery.theme.My4
import com.pavlov.MyShadowGallery.theme.My5
import com.pavlov.MyShadowGallery.theme.My7
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomButtonOne
import com.pavlov.MyShadowGallery.theme.uiComponents.CustomOutlinedTextField
import com.pavlov.MyShadowGallery.theme.uiComponents.MatrixBackground

@Composable
fun SetPasswordScreen(
    viewModel: SetPasswordViewModel = hiltViewModel(),
    onPasswordSet: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        content = { padding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            MatrixBackground(255)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {

                                CustomOutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = stringResource(R.string.new_password),
                                    placeholder = stringResource(R.string.enter_new_password_placeholder),
                                    backgroundColor = My4,
                                    isPassword = true, // Предполагается, что поле пароля скрывает ввод
                                    keyboardActions = { ImeAction.Done }
                                )

                    Spacer(modifier = Modifier.height(8.dp))

                    CustomOutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = stringResource(R.string.confirm_password),
                        placeholder = stringResource(R.string.confirm_new_password_placeholder),
                        backgroundColor = My4,
                        isPassword = true, // Предполагается, что поле пароля скрывает ввод
                        keyboardActions = {
                            if (newPassword == confirmPassword) {
                                viewModel.savePassword(newPassword)
                                onPasswordSet()
                            } else {
                                message = context.getString(R.string.passwords_do_not_match)
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
                                message = context.getString(R.string.passwords_do_not_match)
                            }
                        },
                        text = stringResource(R.string.enter),
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

