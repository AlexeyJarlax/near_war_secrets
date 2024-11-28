package com.pavlov.nearWarSecrets.theme.uiComponents

import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import androidx.compose.ui.res.colorResource
import com.pavlov.nearWarSecrets.R

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    val checkboxColors = CheckboxDefaults.colors(
        checkedColor = colorResource(id = R.color.my_prime_day),
        uncheckedColor = Color.Gray,
        checkmarkColor = Color.White,
    )

    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = checkboxColors,
        enabled = enabled
    )
}

@Preview
@Composable
fun PreviewCustomCheckbox() {
    var checked by remember { mutableStateOf(false) }

    CustomCheckbox(
        checked = checked,
        onCheckedChange = { checked = it },
        enabled = true
    )
}