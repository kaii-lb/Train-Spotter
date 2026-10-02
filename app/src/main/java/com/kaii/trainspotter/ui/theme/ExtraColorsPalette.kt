package com.kaii.trainspotter.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalExtraColorsPalette = staticCompositionLocalOf { ExtraColorsPalette() }

@Immutable
data class ExtraColorsPalette(
    val success: Color = com.kaii.trainspotter.ui.theme.success,
    val onSuccess: Color = com.kaii.trainspotter.ui.theme.onSuccess
)