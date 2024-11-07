package com.pavlov.MyShadowGallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pavlov.MyShadowGallery.file.StorageLogActivity
import com.pavlov.MyShadowGallery.security.KeyInputActivity
import com.pavlov.MyShadowGallery.security.ThreeStepsActivity
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM
import com.pavlov.MyShadowGallery.util.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MainPageScreen() {
    val context = LocalContext.current

    // Получаем фоновое изображение из ThemeManager
    val backgroundResource = ThemeManager.applyUserSwitch(context)

    // Инициализируем APKM для управления настройками
    val apkManager = remember { APKM(context) }

    // Состояния для символов безопасности
    var simblPass by remember { mutableStateOf("🏳️") }
    var simblMimic by remember { mutableStateOf("🏳️") }
    var simblEncryption by remember { mutableStateOf("🏳️") }

    // Обновляем символы при запуске
    LaunchedEffect(Unit) {
        locker(apkManager) { pass, mimic, encryption ->
            simblPass = pass
            simblMimic = mimic
            simblEncryption = encryption
        }
    }

    // Проверяем необходимость запуска KeyInputActivity
    LaunchedEffect(Unit) {
        prestart(apkManager, context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = backgroundResource),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Основное содержимое
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Верхняя панель с заголовком и кнопкой входа
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.app_name_in_main_page),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onBackground
                )
                IconButton(
                    onClick = {
                        val displayIntent = Intent(context, AboutActivity::class.java)
                        context.startActivity(displayIntent)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_get),
                        contentDescription = null,
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопки основного меню
            MainMenuButton(
                text = stringResource(R.string.button_item_loader),
                iconRes = android.R.drawable.ic_menu_add,
                onClick = {
                    val displayIntent = Intent(context, ItemLoaderActivity::class.java)
                    context.startActivity(displayIntent)
                }
            )

            MainMenuButton(
                text = stringResource(R.string.storage),
                iconRes = android.R.drawable.ic_dialog_dialer,
                onClick = {
                    val displayIntent = Intent(context, ItemLoaderActivity::class.java)
                    context.startActivity(displayIntent)
                }
            )

            MainMenuButton(
                text = stringResource(R.string.storage_log),
                iconRes = android.R.drawable.ic_input_get,
                onClick = {
                    val displayIntent = Intent(context, StorageLogActivity::class.java)
                    context.startActivity(displayIntent)
                }
            )

            // Кнопка "Как это работает"
            MainMenuButton(
                text = stringResource(R.string.how_does_is_work),
                iconRes = android.R.drawable.ic_menu_help,
                onClick = {
                    val displayIntent = Intent(context, FAQActivity::class.java)
                    context.startActivity(displayIntent)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Нижняя панель с настройками и кнопками безопасности
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Кнопка настроек
                Button(
                    onClick = {
                        val displayIntent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(displayIntent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_preferences),
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.button_settings),
                        color = MaterialTheme.colors.onSurface
                    )
                }

                // Кнопки безопасности
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    SecurityButton(
                        text = simblEncryption,
                        onClick = {
                            val displayIntent = Intent(context, ThreeStepsActivity::class.java)
                            displayIntent.putExtra("buttonSecurity3", true)
                            context.startActivity(displayIntent)
                        },
                        backgroundColor = getBackgroundColor(simblEncryption)
                    )

                    SecurityButton(
                        text = simblPass,
                        onClick = {
                            val displayIntent = Intent(context, ThreeStepsActivity::class.java)
                            displayIntent.putExtra("buttonSecurity1", true)
                            context.startActivity(displayIntent)
                        },
                        backgroundColor = getBackgroundColor(simblPass)
                    )

                    SecurityButton(
                        text = simblMimic,
                        onClick = {
                            val displayIntent = Intent(context, ThreeStepsActivity::class.java)
                            displayIntent.putExtra("buttonSecurity2", true)
                            context.startActivity(displayIntent)
                        },
                        backgroundColor = getBackgroundColor(simblMimic)
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuButton(text: String, iconRes: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = MaterialTheme.colors.onSurface)
    }
}

@Composable
fun SecurityButton(text: String, onClick: () -> Unit, backgroundColor: Color) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor)
    ) {
        Text(text = text, color = MaterialTheme.colors.onPrimary)
    }
}

@Composable
fun getBackgroundColor(symbol: String): Color {
    return if (symbol != "🏳️") {
        // Цвет, когда настройка активна
        MaterialTheme.colors.primary
    } else {
        // Цвет, когда настройка неактивна
        MaterialTheme.colors.secondary
    }
}

suspend fun locker(apkManager: APKM, updateSymbols: (String, String, String) -> Unit) {
    val passKey = apkManager.getBooleanFromSPK(APK.KEY_EXIST_OF_PASSWORD, false)
    val encryptionKeyName = apkManager.getIntFromSP(APK.DEFAULT_KEY) != 0
    val mimikKey = apkManager.getBooleanFromSPK(APK.KEY_EXIST_OF_MIMICRY, false)

    // Обновляем символы безопасности в основном потоке
    withContext(Dispatchers.Main) {
        val simblPass = if (passKey) "🔐" else "🏳️"
        val simblMimic = if (mimikKey) "🕶️" else "🏳️"
        val simblEncryption = if (encryptionKeyName) apkManager.getDefauldKeyName() else "🏳️"

        updateSymbols(simblPass, simblMimic, simblEncryption)
    }
}

fun prestart(apkManager: APKM, context: Context) {
    val pref1 = apkManager.getBooleanFromSPK(APK.KEY_DELETE_AFTER_SESSION, false)
    val pref2 = apkManager.getBooleanFromSPK(APK.KEY_EXIST_OF_ENCRYPTION_K, false)
    if (pref1 && !pref2) {
        val displayIntent = Intent(context, KeyInputActivity::class.java)
        context.startActivity(displayIntent)
        if (context is Activity) {
            context.finish()
        }
    }
}