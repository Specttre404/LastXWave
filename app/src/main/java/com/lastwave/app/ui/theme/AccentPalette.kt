package com.lastwave.app.ui.theme

import androidx.compose.ui.graphics.Color

sealed class AppAccent(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val container: Color,
    val surfaceTint: Color,
    val background: Color = Color(0xFF0C0D0E)
) {
    // 1. Amber Tube: Warm thermionic glowing vacuum-tube orange/amber
    data object AmberTube : AppAccent(
        id = "amber_tube",
        name = "Amber Tube",
        primary = Color(0xFFFF8C00),     // Thermionic Glow Orange
        secondary = Color(0xFFFFB703),   // Filament Amber
        container = Color(0xFF331A05),   // Warm Mahogany Glass
        surfaceTint = Color(0xFFFFA000),
        background = Color(0xFF180E04)
    )

    // 2. Obsidian Gold: Pure obsidian black chassis with 24K champagne gold
    data object ObsidianGold : AppAccent(
        id = "obsidian_gold",
        name = "Obsidian Gold",
        primary = Color(0xFFD4AF37),     // 24K Metallic Champagne Gold
        secondary = Color(0xFFFFF275),   // Pale Gold Sheen
        container = Color(0xFF141518),   // Deep Obsidian Monolith
        surfaceTint = Color(0xFFE5C158),
        background = Color(0xFF0C0D0E)   // Obsidian Black Chassis
    )

    data object ElectricCyan : AppAccent("electric_cyan", "Electric Cyan", Color(0xFF00E5FF), Color(0xFF0072FF), Color(0xFF05212A), Color(0xFF00F0FF))
    data object NeonAmethyst : AppAccent("neon_amethyst", "Neon Amethyst", Color(0xFFB5179E), Color(0xFF7209B7), Color(0xFF220826), Color(0xFFE0AAFF))
    data object EmeraldHiFi : AppAccent("emerald_hifi", "Emerald HiFi", Color(0xFF06D6A0), Color(0xFF118AB2), Color(0xFF042018), Color(0xFF38EF7D))

    companion object {
        val ALL_ACCENTS = listOf(AmberTube, ObsidianGold, ElectricCyan, NeonAmethyst, EmeraldHiFi)
        fun fromId(id: String?): AppAccent = ALL_ACCENTS.firstOrNull { it.id == id } ?: ObsidianGold
    }
}
