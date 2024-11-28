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
    const val rows = 30 // количество дорожек с символами
    const val maxVisibleSymbols = 70 // Максимальное количество видимых символов
    const val symbolDelay = 200L // Задержка между появлениями символов (в миллисекундах)
    const val fadeStep = 0.06f // Шаг уменьшения альфы
    const val alphaStart = 1f // Начальное значение альфы
    const val maxYOffset = 100 // Максимальное вертикальное смещение
    const val maxXOffset = 10 // Максимальное горизонтальное смещение (в пикселях)
    const val maxDelay = 25000L // Макс задержка в миллисек
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

    // Получаем ширину экрана
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenWidthPx = with(LocalDensity.current) { screenWidth.toInt() }

    // Генерируем случайное горизонтальное смещение
    val randomXOffset = (Random.nextInt(1, 21) * 20)
    val randomStartDelay = Random.nextLong(100L, MatrixAnimationSettings.maxDelay)

    LaunchedEffect(Unit) {
        delay(randomStartDelay) // Задержка перед началом анимации

        // Создаем анимацию для каждого символа
        while (animationRunning && isActive) {
            // Задержка между появлениями символов
            delay(MatrixAnimationSettings.symbolDelay)

            // Создание нового символа с оптимизацией на память
            val newSymbol = MatrixSymbol(
                symbol = symbols.random(),
                index = Random.nextInt(0, 1000),
                alpha = MatrixAnimationSettings.alphaStart,
                yOffset = symbolList.size * 20,
                xOffset = randomXOffset
            )

            // Обновление состояния списка символов без блокировки UI
            symbolList = symbolList + newSymbol

            // Уменьшаем альфу у старых символов (сделано с использованием map для оптимизации)
            symbolList = symbolList.mapIndexed { index, symbol ->
                symbol.copy(alpha = symbol.alpha - MatrixAnimationSettings.fadeStep)
            }

            // Удаляем старые символы после определенного количества
            if (symbolList.size > MatrixAnimationSettings.maxVisibleSymbols) {
                symbolList = symbolList.drop(1)
            }

            // Завершаем анимацию, если все символы исчезли (alpha достигло 0)
            if (symbolList.all { it.alpha <= 0f }) {
                animationRunning = false // Останавливаем анимацию
                symbolList = emptyList() // Освобождаем память
            }
        }
    }

    // Рендерим каждый символ
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
    val yOffset: Int, // Позиция по вертикали
    val xOffset: Int // Позиция по горизонтали
)