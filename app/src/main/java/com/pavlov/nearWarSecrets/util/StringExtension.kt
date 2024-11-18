package com.pavlovalexey.pleinair.utils

import java.time.LocalDate
import java.time.LocalTime

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this)
}

fun String.toLocalTime(): LocalTime {
    return LocalTime.parse(this)
}