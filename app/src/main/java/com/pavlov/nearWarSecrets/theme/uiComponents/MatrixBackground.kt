package com.pavlov.nearWarSecrets.theme.uiComponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlin.random.Random

// Настройки анимации и символов
object MatrixAnimationSettings {
    val symbols = listOf(
        'ア', 'ィ', 'イ', 'ゥ', 'ウ', 'ェ', 'エ', 'ォ', 'オ', 'カ', 'ガ', 'キ', 'ギ', 'ク', 'グ', 'ケ', 'ゲ', 'コ', 'ゴ',
        'サ', 'ザ', 'シ', 'ジ', 'ス', 'ズ', 'セ', 'ゼ', 'ソ', 'ゾ', 'タ', 'ダ', 'チ', 'ヂ', 'ッ', 'ツ', 'ヅ', 'テ', 'デ',
        'ト', 'ド', 'ナ', 'ニ', 'ヌ', 'ネ', 'ノ', 'ハ', 'バ', 'パ', 'ヒ', 'ビ', 'ピ', 'フ', 'ブ', 'プ', 'ヘ', 'ベ', 'ペ',
        'ホ', 'ボ', 'ポ', 'マ', 'ミ', 'ム', 'メ', 'モ', 'ャ', 'ヤ', 'ュ', 'ユ', 'ョ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ',
        'ヮ', 'ワ', 'ヰ', 'ヱ', 'ヲ', 'ン', 'ヴ', 'ヵ', 'ヶ', 'ヷ', 'ヸ', 'ヹ', 'ヺ', '・', 'ー', 'ヽ', 'ヾ'
    )
    const val rows = 35            // Количество колонок
    const val maxVisibleSymbols = 10
    const val speed = 6f           // Скорость движения символов вниз
    const val fontSize = 14
    // Диапазон задержки для старта каждой колонки
    val columnStartDelayRange = 500L..12000L
    // Отступ между символами
    val symbolPadding = 10.dp
    // Скорость уменьшения альфы при затухании символа
    const val fadeSpeed = 0.05f
}

// Класс символа
data class MatrixSymbol(
    val symbol: Char,
    val alpha: Float,
    val xOffset: Float,
    val yOffset: Float
)

@Composable
fun MatrixBackground() {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { screenHeightDp.dp.toPx() }

    // Рассчитываем размер шрифта и вертикальный шаг между символами
    val textPaint = android.graphics.Paint().apply {
        textSize = with(density) { MatrixAnimationSettings.fontSize.sp.toPx() }
    }
    val symbolPaddingPx = with(density) { MatrixAnimationSettings.symbolPadding.toPx() }
    val verticalStep = textPaint.textSize + symbolPaddingPx

    // Генерация состояний для колонок и их случайные задержки
    val columnOffsets = remember {
        List(MatrixAnimationSettings.rows) {
            Random.nextFloat() * screenWidthPx
        }
    }
    val columnDelays = remember {
        List(MatrixAnimationSettings.rows) {
            Random.nextLong(MatrixAnimationSettings.columnStartDelayRange.first, MatrixAnimationSettings.columnStartDelayRange.last)
        }
    }

    val columnsState = remember {
        columnOffsets.mapIndexed { index, startX ->
            generateColumnSymbols(index, startX, verticalStep).toMutableStateList()
        }.toMutableStateList()
    }

    // Для каждой колонки запускаем отдельный LaunchedEffect, который ждёт задержку и начинает обновлять
    columnsState.forEachIndexed { columnIndex, column ->
        LaunchedEffect(columnIndex) {
            // Ждём индивидуальную задержку для данной колонки
            delay(columnDelays[columnIndex])
            while (isActive) {
                withFrameNanos {
                    updateColumnSymbols(column, screenHeightPx)
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GREEN
            textSize = MatrixAnimationSettings.fontSize.sp.toPx()
            isAntiAlias = true
        }

        // Отрисовка символов
        columnsState.forEach { columnSymbols ->
            columnSymbols.forEach { symbol ->
                if (symbol.alpha > 0f) {
                    paint.alpha = (symbol.alpha * 255).coerceIn(0f, 255f).toInt()
                    drawContext.canvas.nativeCanvas.drawText(
                        symbol.symbol.toString(),
                        symbol.xOffset,
                        symbol.yOffset,
                        paint
                    )
                }
            }
        }
    }
}

// Генерация символов для одной колонки с учетом verticalStep
fun generateColumnSymbols(
    columnIndex: Int,
    startX: Float,
    verticalStep: Float
): MutableList<MatrixSymbol> {
    val list = mutableListOf<MatrixSymbol>()
    repeat(MatrixAnimationSettings.maxVisibleSymbols) { i ->
        list.add(
            MatrixSymbol(
                symbol = MatrixAnimationSettings.symbols.random(),
                alpha = 0f,
                xOffset = startX,
                yOffset = -(i * verticalStep)
            )
        )
    }
    return list
}

// Обновляем символы одной колонки с учетом fadeSpeed
fun updateColumnSymbols(
    columnSymbols: MutableList<MatrixSymbol>,
    screenHeight: Float
) {
    val newList = mutableListOf<MatrixSymbol>()

    for (symbol in columnSymbols) {
        var y = symbol.yOffset + MatrixAnimationSettings.speed
        var a = symbol.alpha
        var s = symbol.symbol

        val normalizedPos = y / screenHeight

        a = when {
            normalizedPos < 0f -> {
                0f
            }
            normalizedPos in 0f..0.3f -> {
                (normalizedPos / 0.3f).coerceIn(0f, 1f)
            }
            normalizedPos in 0.3f..0.7f -> {
                1f
            }
            else -> {
                // Затухание с использованием fadeSpeed
                val fadeFraction = ((normalizedPos - 0.7f) / 0.3f).coerceAtLeast(0f)
                val alphaWithoutFadeSpeed = (1 - fadeFraction).coerceIn(0f,1f)
                (alphaWithoutFadeSpeed - MatrixAnimationSettings.fadeSpeed).coerceAtLeast(0f)
            }
        }

        // Если символ вышел за нижнюю границу или его альфа <= 0f, перезапускаем символ сверху
        if (y > screenHeight || a <= 0f) {
            y = Random.nextFloat() * (-100f)
            s = MatrixAnimationSettings.symbols.random()
            a = 0f
        }

        newList.add(symbol.copy(symbol = s, alpha = a, yOffset = y))
    }

    // Обновляем список символов, чтобы Compose отреагировал на изменение состояния
    for (i in columnSymbols.indices) {
        columnSymbols[i] = newList[i]
    }
}
