package com.pavlov.nearWarSecrets.theme.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

// Настройки для анимации
object MatrixAnimationSettings {
    val symbols = listOf(
        'ア', 'ィ', 'イ', 'ゥ', 'ウ', 'ェ', 'エ', 'ォ', 'オ', 'カ', 'ガ', 'キ', 'ギ', 'ク', 'グ', 'ケ', 'ゲ', 'コ', 'ゴ',
        'サ', 'ザ', 'シ', 'ジ', 'ス', 'ズ', 'セ', 'ゼ', 'ソ', 'ゾ', 'タ', 'ダ', 'チ', 'ヂ', 'ッ', 'ツ', 'ヅ', 'テ', 'デ',
        'ト', 'ド', 'ナ', 'ニ', 'ヌ', 'ネ', 'ノ', 'ハ', 'バ', 'パ', 'ヒ', 'ビ', 'ピ', 'フ', 'ブ', 'プ', 'ヘ', 'ベ', 'ペ',
        'ホ', 'ボ', 'ポ', 'マ', 'ミ', 'ム', 'メ', 'モ', 'ャ', 'ヤ', 'ュ', 'ユ', 'ョ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ',
        'ヮ', 'ワ', 'ヰ', 'ヱ', 'ヲ', 'ン', 'ヴ', 'ヵ', 'ヶ', 'ヷ', 'ヸ', 'ヹ', 'ヺ', '・', 'ー', 'ヽ', 'ヾ'
    )
    const val rows = 5 // количество дорожек с символами
    const val maxVisibleSymbols = 70 // Максимальное количество видимых символов
    const val symbolDelay = 200L // Задержка между появлениями символов (в миллисекундах)
    const val fadeStep = 0.1f // Шаг уменьшения альфы
    const val alphaStart = 1f // Начальное значение альфы
    const val maxYOffset = 100 // Максимальное вертикальное смещение
    const val maxXOffset = 10 // Максимальное горизонтальное смещение (в пикселях)
    const val maxDelay = 10000L // Макс задержка в миллисек
    const val fontSize = 12 // Размер шрифта
    var symbolPadding = 1.dp // Отступ между символами
}

@Composable
fun MatrixBackground() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        // Каждый столбец символов (поток)
        for (i in 0 until MatrixAnimationSettings.rows) {
            MatrixColumn(MatrixAnimationSettings.symbols, i, MatrixAnimationSettings.fontSize)
        }
    }
}

@Composable
fun MatrixColumn(symbols: List<Char>, columnIndex: Int, fontSize: Int) {
    var symbolList by remember { mutableStateOf(listOf<MatrixSymbol>()) }
    var animationRunning by remember { mutableStateOf(true) } // Флаг анимации

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenWidthPx = with(LocalDensity.current) { screenWidth.toInt() }

    val randomXOffset = (Random.nextInt(1, 21) * 20)
    val randomStartDelay = Random.nextLong(100L, MatrixAnimationSettings.maxDelay)

    LaunchedEffect(Unit) {
        delay(randomStartDelay)

        while (animationRunning && isActive) {

            delay(MatrixAnimationSettings.symbolDelay)

            val newSymbol = MatrixSymbol(
                symbol = symbols.random(),
                index = Random.nextInt(0, 1000),
                alpha = MatrixAnimationSettings.alphaStart,
                yOffset = symbolList.size * 20,
                xOffset = randomXOffset
            )

            symbolList = symbolList + newSymbol
            symbolList = symbolList.mapIndexed { index, symbol ->
                symbol.copy(alpha = symbol.alpha - MatrixAnimationSettings.fadeStep)
            }
            if (symbolList.size > MatrixAnimationSettings.maxVisibleSymbols) {
                symbolList = symbolList.drop(1)
            }
            if (symbolList.all { it.alpha <= 0f }) {
                animationRunning = false
                symbolList = emptyList()
            }
        }
    }

    symbolList.forEach { symbol ->
        Text(
            text = symbol.symbol.toString(),
            color = Color.Green.copy(alpha = symbol.alpha),
            fontSize = fontSize.sp,
            modifier = Modifier
                .padding(MatrixAnimationSettings.symbolPadding)
                .offset(x = symbol.xOffset.dp, y = symbol.yOffset.dp)
        )
    }
}

data class MatrixSymbol(
    val symbol: Char,
    val index: Int,
    val alpha: Float,
    val yOffset: Int,
    val xOffset: Int
)