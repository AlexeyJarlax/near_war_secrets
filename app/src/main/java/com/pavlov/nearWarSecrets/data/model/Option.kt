package com.pavlov.nearWarSecrets.data.model

/** выбор в FilterDialog, SelectionDialog для аватарки и прочие аналогичные диалоги выбора из опций*/

import androidx.annotation.Keep

@Keep
data class Option(
    val id: Int,
    val iconRes: Int,
    val text: String
)