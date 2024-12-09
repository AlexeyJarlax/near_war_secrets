package com.pavlov.MyShadowGallery.theme.uiComponents

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

object MatrixAnimationSettings {
    val symbols = listOf(
        'ア', 'ィ', 'イ', 'ゥ', 'ウ', 'ェ', 'エ', 'ォ', 'オ', 'カ', 'ガ', 'キ', 'ギ', 'ク', 'グ', 'ケ', 'ゲ', 'コ', 'ゴ',
        'サ', 'ザ', 'シ', 'ジ', 'ス', 'ズ', 'セ', 'ゼ', 'ソ', 'ゾ', 'タ', 'ダ', 'チ', 'ヂ', 'ッ', 'ツ', 'ヅ', 'テ', 'デ',
        'ト', 'ド', 'ナ', 'ニ', 'ヌ', 'ネ', 'ノ', 'ハ', 'バ', 'パ', 'ヒ', 'ビ', 'ピ', 'フ', 'ブ', 'プ', 'ヘ', 'ベ', 'ペ',
        'ホ', 'ボ', 'ポ', 'マ', 'ミ', 'ム', 'メ', 'モ', 'ャ', 'ヤ', 'ュ', 'ユ', 'ョ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ',
        'ヮ', 'ワ', 'ヰ', 'ヱ', 'ヲ', 'ン', 'ヴ', 'ヵ', 'ヶ', 'ヷ', 'ヸ', 'ヹ', 'ヺ', '・', 'ー', 'ヽ', 'ヾ'
    )
    const val rows = 18
    const val fontSize = 14
    val symbolPadding = 1.dp
    val columnStartDelayRange = 50L..20000L
    const val updateInterval = 250L // Интервал обновления (мс)
    const val fadeSpeed = 0.05f     // Скорость затухания альфы за "тик"
}

data class MatrixSymbol(
    val symbol: Char,
    val yPos: Float,
    val xPos: Float,
    val alpha: Float
)

@Composable
fun MatrixBackground(greencomponent: Int = 155) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val textSizePx = with(density) { MatrixAnimationSettings.fontSize.sp.toPx() }
    val symbolPaddingPx = with(density) { MatrixAnimationSettings.symbolPadding.toPx() }
    val verticalStep = textSizePx + symbolPaddingPx

    val paint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
        }
    }
    paint.textSize = textSizePx

    // Чтобы символы не накладывались друг на друга по горизонтали,
    // используем дискретные позиции. Возьмем шаг по горизонтали кратный размеру символа.
    val horizontalSpacing = textSizePx * 1.5f // коэффициент, чтобы символы не соприкасались
    val maxColumnsFit = (screenWidthPx / horizontalSpacing).toInt()

    // Если на экране колонок больше, чем можно разместить без наложения,
    // можно либо уменьшить число колонок, либо уменьшить spacing.
    val columnsCount = MatrixAnimationSettings.rows
    val actualColumnsCount = if (columnsCount <= maxColumnsFit) columnsCount else maxColumnsFit

    // Генерируем уникальные позиции (слоты) для колонок
    val availableSlots = (0 until maxColumnsFit).toList().shuffled().take(actualColumnsCount)

    // Если columnsCount > actualColumnsCount, часть колонок не поместится корректно,
    // но предположим, что rows <= maxColumnsFit для данной конфигурации экрана.
    // Распределяем колонки по выбранным слотам
    val columnOffsets = remember {
        availableSlots.map { slotIndex ->
            slotIndex * horizontalSpacing
        }
    }

    // Задержки для каждой колонки (можно делать меньше колонок, если columnsCount > actualColumnsCount)
    val columnDelays = remember {
        List(actualColumnsCount) {
            Random.nextLong(MatrixAnimationSettings.columnStartDelayRange.first, MatrixAnimationSettings.columnStartDelayRange.last)
        }
    }

    // Состояния колонок
    val columnsState = remember {
        columnOffsets.map { xPos ->
            mutableStateListOf<MatrixSymbol>()
        }.toMutableStateList()
    }

    val columnHeights = remember {
        MutableList(actualColumnsCount) { 0 }
    }

    // Запускаем корутины для каждой колонки
    columnsState.forEachIndexed { columnIndex, column ->
        LaunchedEffect(columnIndex) {
            delay(columnDelays[columnIndex]) // индивидуальная задержка старта
            while (isActive) {
                val nextIndex = columnHeights[columnIndex] + 1
                val yPos = nextIndex * verticalStep
                val newSymbolChar = MatrixAnimationSettings.symbols.random()
                column.add(
                    MatrixSymbol(
                        symbol = newSymbolChar,
                        yPos = yPos,
                        xPos = columnOffsets[columnIndex],
                        alpha = 1f
                    )
                )
                columnHeights[columnIndex] = nextIndex

                // Обновляем альфы всех символов
                val updated = column.mapNotNull { sym ->
                    val newAlpha = sym.alpha - MatrixAnimationSettings.fadeSpeed
                    if (newAlpha > 0f) sym.copy(alpha = newAlpha) else null
                }.toMutableList()

                column.clear()
                column.addAll(updated)

                delay(MatrixAnimationSettings.updateInterval)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        columnsState.forEach { columnSymbols ->
            for (symbol in columnSymbols) {
                val alphaInt = (symbol.alpha * 255).toInt().coerceIn(0,255)
                paint.color = android.graphics.Color.argb(
                    alphaInt,
                    0,
                    greencomponent,
                    0
                )
                drawContext.canvas.nativeCanvas.drawText(
                    symbol.symbol.toString(),
                    symbol.xPos,
                    symbol.yPos,
                    paint
                )
            }
        }
    }
}
