// MatrixBackground.kt

package com.pavlov.nearWarSecrets.ui.theme.uiComponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.pavlov.nearWarSecrets.ui.theme.MyCustom
import kotlin.random.Random

@Composable
fun MatrixBackground() {
    val symbolSize = 16f
    var canvasHeight by remember { mutableStateOf(0f) }
    val streams = remember { mutableStateListOf<Stream>() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        canvasHeight = size.height
        val width = size.width
        if (streams.isEmpty()) {
            val columns = (width / symbolSize).toInt()
            streams.addAll(
                List(columns) { index ->
                    val x = index * symbolSize
                    val y = Random.nextFloat() * -1000f
                    Stream().apply {
                        generateSymbols(x, y, symbolSize)
                    }
                }
            )
        }
        // Отрисовываем потоки
        streams.forEach { stream ->
            stream.render(this, symbolSize)
        }
    }

    // Обновление символов с использованием withFrameNanos
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                if (canvasHeight > 0f) {
                    streams.forEach { stream ->
                        stream.updateSymbols(canvasHeight)
                    }
                }
            }
        }
    }
}

class Symbol(
    val x: Float,
    var y: Float,
    val speed: Float,
    val symbolSize: Float
) {
    var value: Char = ' '
    private var switchInterval = Random.nextInt(2, 20)
    private var timer = 0

    init {
        setRandomSymbol()
    }

    // Метод для смены символа
    fun setRandomSymbol() {
        if (timer % switchInterval == 0) {
            value = (0x30A0 + Random.nextInt(0, 96)).toChar()
        }
    }

    // Метод для падения символа
    fun rain(height: Float) {
        y = if (y >= height) 0f else y + speed
        timer++
        setRandomSymbol()
    }
}

class Stream {
    private val symbols = mutableListOf<Symbol>()
    private val streamLength = Random.nextInt(5, 15)
    private val speed = Random.nextFloat() * 5f + 5f
    private var initialized = false

    // Генерация символов в потоке
    fun generateSymbols(x: Float, y: Float, symbolSize: Float) {
        if (initialized) return
        var posY = y
        repeat(streamLength) {
            val symbol = Symbol(x, posY, speed, symbolSize)
            symbols.add(symbol)
            posY -= symbolSize
        }
        initialized = true
    }

    // Обновление символов
    fun updateSymbols(height: Float) {
        symbols.forEach { symbol ->
            symbol.rain(height)
        }
    }

    // Отрисовка потока
    fun render(
        drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
        symbolSize: Float
    ) {
        drawScope.drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas

            symbols.forEach { symbol ->
                val textPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = symbolSize
                    color = MyCustom.toArgb()
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                nativeCanvas.drawText(
                    symbol.value.toString(),
                    symbol.x,
                    symbol.y,
                    textPaint
                )
            }
        }
    }
}
