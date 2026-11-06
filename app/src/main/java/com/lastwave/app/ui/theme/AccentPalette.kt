package com.lastwave.app.ui.theme

import androidx.compose.ui.graphics.Color

sealed class AppAccent(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val container: Color,
    val surfaceTint: Color,
    val glassBorder: Color = Color(0x3300E5FF),
    val background: Color = Color(0xFF0B0F1A)
) {
    // 1. Material 3 Expressive: Fixed expressive dynamic palette using Violet/Purple
    data object Material3Expressive : AppAccent(
        id = "m3_expressive",
        name = "Material 3 Expressive",
        primary = Color(0xFF7F52FF),
        secondary = Color(0xFFFF2E93),
        container = Color(0xFF220826),
        surfaceTint = Color(0xFFE0AAFF),
        glassBorder = Color(0x337F52FF),
        background = Color(0xFF0F0B1A)
    )

    // 2. iOS Liquid Glass: Fixed specular frosted tint using Cyber Cyan
    data object LiquidGlass : AppAccent(
        id = "liquid_glass",
        name = "iOS Liquid Glass",
        primary = Color(0xFF00E5FF),
        secondary = Color(0xFF7F52FF),
        container = Color(0xFF05212A),
        surfaceTint = Color(0xFF00F0FF),
        glassBorder = Color(0x3300E5FF),
        background = Color(0xFF05070B)
    )

    companion object {
        val ALL_ACCENTS = listOf(Material3Expressive, LiquidGlass)
        fun fromId(id: String?): AppAccent = if (id == "m3_expressive") Material3Expressive else LiquidGlass
    }
}
