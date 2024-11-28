package com.pavlov.nearWarSecrets.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pavlov.nearWarSecrets.R
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.pavlov.nearWarSecrets.theme.uiComponents.MatrixBackground

@Composable
fun SettingsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onAboutClicked: () -> Unit,
    onSecuritySettingsClicked: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val previewSize by viewModel.previewSize.collectAsState()
    val personalDataText by viewModel.personalDataText.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MatrixBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Preview size slider
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.preview_size_label, previewSize, previewSize))
            Slider(
                value = previewSize.toFloat(),
                onValueChange = { viewModel.updatePreviewSize(it.toInt()) },
                valueRange = 1f..10f
            )
        }

        // Image previews
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.m100),
                contentDescription = null,
                modifier = Modifier.size(previewSize.dp)
            )
            Image(
                painter = painterResource(R.drawable.c100),
                contentDescription = null,
                modifier = Modifier.size(previewSize.dp)
            )
            Image(
                painter = painterResource(R.drawable.t100),
                contentDescription = null,
                modifier = Modifier.size(previewSize.dp)
            )
        }

        // Buttons
        Button(onClick = { viewModel.clearStorage() }) {
            Text(text = stringResource(R.string.clearing_the_storage))
        }

        Button(onClick = { viewModel.resetSettings() }) {
            Text(text = stringResource(R.string.reset_settings))
        }

        Button(onClick = onAboutClicked) {
            Text(text = stringResource(R.string.about_the_app))
        }

        Button(onClick = { viewModel.togglePersonalData() }) {
            Text(text = personalDataText)
        }

        Button(onClick = onSecuritySettingsClicked) {
            Text(text = stringResource(R.string.security_settings))
        }

        // Back button
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onNavigateBack) {
            Text(text = stringResource(R.string.back))
        }
    }
}}