package com.pavlov.nearWarSecrets.ui.theme.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavlov.nearWarSecrets.ui.theme.uiComponents.MatrixAnimationSettings.fontSize
import com.pavlov.nearWarSecrets.ui.theme.uiComponents.MatrixAnimationSettings.maxDelay
import kotlinx.coroutines.delay
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
    const val rows = 40 // количество дорожек с символами
    const val maxVisibleSymbols = 100 // Максимальное количество видимых символов
    const val symbolDelay = 200L // Задержка между появлениями символов (в миллисекундах)
    const val fadeStep = 0.06f // Шаг уменьшения альфы
    const val alphaStart = 1f // Начальное значение альфы
    const val maxYOffset = 100 // Максимальное вертикальное смещение
    const val maxXOffset = 600 // Максимальное горизонтальное смещение (в пикселях)
    const val maxDelay = 30000L // Макс задержка в миллисек
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
            MatrixColumn(MatrixAnimationSettings.symbols, i, fontSize)
        }
    }
}

@Composable
fun MatrixColumn(symbols: List<Char>, columnIndex: Int, fontSize: Int) {
    var symbolList by remember { mutableStateOf(listOf<MatrixSymbol>()) }

    // Получаем ширину экрана
    val screenWidth = LocalConfiguration.current.screenWidthDp

    // Конвертируем screenWidth в пиксели
    val screenWidthPx = with(LocalDensity.current) { screenWidth.toInt() }

    // Генерируем случайное горизонтальное смещение относительно центра экрана
    val randomXOffset = (Random.nextInt(1, 21) * 20) - (screenWidthPx / 2)

    // Случайная задержка старта в пределах от 1 до 30 секунд
    val randomStartDelay = Random.nextLong(100L, MatrixAnimationSettings.maxDelay)

    LaunchedEffect(Unit) {
        delay(randomStartDelay) // Задержка перед началом анимации

        // Создаем анимацию для каждого символа, который будет появляться
        while (true) {
            delay(MatrixAnimationSettings.symbolDelay) // Задержка между появлениями символов
            val isFirstSymbol = symbolList.isEmpty() // Проверяем, первый ли это символ в колонке
            val newSymbol = MatrixSymbol(
                symbol = symbols.random(),
                index = Random.nextInt(0, 1000),
                alpha = MatrixAnimationSettings.alphaStart,
                yOffset = symbolList.size * 20, // Вертикальное смещение увеличиваем с каждым новым символом
                xOffset = randomXOffset // Применяем случайное горизонтальное смещение
            )
            symbolList = symbolList + newSymbol

            // Уменьшаем альфу у старых символов
            symbolList = symbolList.mapIndexed { index, symbol ->
                symbol.copy(alpha = symbol.alpha - MatrixAnimationSettings.fadeStep)
            }

            // Удаляем старые символы, чтобы список не растягивался бесконечно
            if (symbolList.size > MatrixAnimationSettings.maxVisibleSymbols) {
                symbolList = symbolList.drop(1)
            }
        }
    }

    // Рендерим каждый символ в столбце с анимацией, на соответствующей вертикальной позиции
    symbolList.forEach { symbol ->
        Text(
            text = symbol.symbol.toString(),
            color = Color.Green.copy(alpha = symbol.alpha),
            fontSize = fontSize.sp, // Используем выбранный размер шрифта
            modifier = Modifier
                .padding(MatrixAnimationSettings.symbolPadding)
                .offset(x = symbol.xOffset.dp, y = symbol.yOffset.dp) // Применяем смещение по X для каждой колонки
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