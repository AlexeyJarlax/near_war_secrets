package com.pavlov.MyShadowGallery.theme.uiComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pavlov.MyShadowGallery.theme.My1
import com.pavlov.MyShadowGallery.theme.My3
import com.pavlov.MyShadowGallery.theme.My7

class FallingStarsTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val transformedText = "*".repeat(text.text.length)
        return TransformedText(
            AnnotatedString(transformedText),
            OffsetMapping.Identity
        )
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    singleLine: Boolean = true,
    backgroundColor: Color,
    isPassword: Boolean,
    keyboardActions: () -> Unit // Функция, вызываемая при нажатии на галочку
) {
    val transformation = remember { FallingStarsTransformation() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            label = { Text(label, color = if (isError) Color.Red else My7) },
            placeholder = {
                Text(
                    placeholder,
                    color = My3.copy(alpha = 0.6f),
                    textAlign = TextAlign.Start
                )
            },
            isError = isError,
            singleLine = singleLine,
            visualTransformation = transformation,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardActions() }
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = backgroundColor,
                focusedBorderColor = My7,
                unfocusedBorderColor = My1,
                cursorColor = My7,
                textColor = My3,
                errorBorderColor = Color.Red
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

/**
CustomOutlinedTextField(
value = password,
onValueChange = { password = it },
label = "Пароль",
placeholder = "Пример плохого пароля: qwerty123",
backgroundColor = My4,
keyboardActions =  {viewModel.onPasswordEntered(password)}
)
 */