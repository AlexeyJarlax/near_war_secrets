package com.pavlov.nearWarSecrets.util

import android.graphics.Color

object ColorsUtil {
    private val alpha = 230
    private val colors = arrayOf(
        Color.argb(alpha, 128, 0, 0),       // Темно-красный
        Color.argb(alpha, 0, 128, 0),       // Темно-зеленый
        Color.argb(alpha, 0, 0, 128),       // Темно-синий
        Color.argb(alpha, 128, 128, 0),     // Темно-желтый
        Color.argb(alpha, 128, 0, 128),     // Темно-фиолетовый
        Color.argb(alpha, 0, 128, 128),     // Темно-бирюзовый
        Color.argb(alpha, 128, 64, 0),      // Темно-оранжевый
        Color.argb(alpha, 64, 0, 128)       // Темно-сиреневый
    )

    fun getColor(position: Int): Int {
        return colors[position % colors.size]
    }
}