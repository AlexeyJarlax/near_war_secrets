package com.pavlov.nearWarSecrets.theme.uiComponents

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
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pavlov.nearWarSecrets.theme.My1
import com.pavlov.nearWarSecrets.theme.My3
import com.pavlov.nearWarSecrets.theme.My7

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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    backgroundColor: Color,
    keyboardActions: KeyboardActions
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
            keyboardOptions = keyboardOptions,
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
