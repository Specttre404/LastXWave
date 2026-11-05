package com.lastwave.app.ui.theme

import androidx.compose.ui.graphics.Color

val VoidBackground = Color(0xFF05070B)
val SurfaceGlassHigh = Color(0xFF10141C)
val SurfaceGlassLow = Color(0xFF0A0D14)
val NeonCyanPrimary = Color(0xFF00E5FF)
val AudiophileGold = Color(0xFFFFD700)
val ExpressivePurple = Color(0xFF7F52FF)
val SubduedGrayText = Color(0xFF94A3B8)
val GlassBorderStroke = Color(0x2600E5FF) // 15% Cyan stroke for glowing borders

val CinematicVoid = Color(0xFF020408)
val AmbientGlowCyan = Color(0xFF00F0FF)
val RawNeonPurple = Color(0xFF9D00FF)
val HdrGoldAccent = Color(0xFFFFC700)
val GlassSurfacePanel = Color(0xFF0D121F)
val GlassBorderActive = Color(0x4D00F0FF)
val BokehBlurOverlay = Color(0x66020408)

// 1. Amber Tube: Thermionic vacuum-tube warm amber/orange
val AmberTubePrimary = Color(0xFFFF8C00)       // Vivid glowing orange (#FF8C00)
val AmberTubeSecondary = Color(0xFFFFB703)     // Filament amber (#FFB703)
val AmberTubeContainer = Color(0xFF331800)     // Mahogany glow container (#331800)

// 2. Obsidian Gold: Jet black chassis with 24K metallic champagne gold
val ObsidianGoldPrimary = Color(0xFFD4AF37)     // 24K Champagne metallic gold (#D4AF37)
val ObsidianGoldSecondary = Color(0xFFFFF275)   // Pale reflective gold (#FFF275)
val ObsidianGoldContainer = Color(0xFF121316)   // Obsidian chassis black (#121316)

data class AudiophileAccentTheme(
    val id: String,
    val name: String,
    val primaryHex: String,
    val lightHex: String,
    val primaryColor: Color,
)

object AudiophilePalettes {
    val DYNAMIC_MONET = AudiophileAccentTheme(
        id = "monet",
        name = "Dynamic Monet",
        primaryHex = "#6750A4",
        lightHex = "#D0BCFF",
        primaryColor = Color(0xFF6750A4),
    )

    val CATPPUCCIN_MOCHA = AudiophileAccentTheme(
        id = "catppuccin_mocha",
        name = "Catppuccin Mocha",
        primaryHex = "#CBA6F7",
        lightHex = "#F5E0DC",
        primaryColor = Color(0xFFCBA6F7),
    )

    val NORD = AudiophileAccentTheme(
        id = "nord",
        name = "Nord Polar",
        primaryHex = "#88C0D0",
        lightHex = "#81A1C1",
        primaryColor = Color(0xFF88C0D0),
    )

    val DRACULA = AudiophileAccentTheme(
        id = "dracula",
        name = "Dracula",
        primaryHex = "#BD93F9",
        lightHex = "#FF79C6",
        primaryColor = Color(0xFFBD93F9),
    )

    val TOKYO_NIGHT = AudiophileAccentTheme(
        id = "tokyo_night",
        name = "Tokyo Night",
        primaryHex = "#7DCFFF",
        lightHex = "#7AA2F7",
        primaryColor = Color(0xFF7DCFFF),
    )

    val GRUVBOX_DARK = AudiophileAccentTheme(
        id = "gruvbox_dark",
        name = "Gruvbox Dark",
        primaryHex = "#FABD2F",
        lightHex = "#FE8019",
        primaryColor = Color(0xFFFABD2F),
    )

    val OLED_BLACK = AudiophileAccentTheme(
        id = "oled_black",
        name = "OLED Pitch Black",
        primaryHex = "#000000",
        lightHex = "#222222",
        primaryColor = Color(0xFF000000),
    )

    val ELECTRIC_CYAN = AudiophileAccentTheme(
        id = "electric_cyan",
        name = "Electric Cyan",
        primaryHex = "#00E5FF",
        lightHex = "#80F2FF",
        primaryColor = Color(0xFF00E5FF),
    )

    val NEON_AMETHYST = AudiophileAccentTheme(
        id = "neon_amethyst",
        name = "Neon Amethyst",
        primaryHex = "#9D4EDD",
        lightHex = "#C77DFF",
        primaryColor = Color(0xFF9D4EDD),
    )

    val ALL_THEMES = listOf(
        DYNAMIC_MONET,
        CATPPUCCIN_MOCHA,
        NORD,
        DRACULA,
        TOKYO_NIGHT,
        GRUVBOX_DARK,
        OLED_BLACK,
        ELECTRIC_CYAN,
        NEON_AMETHYST,
    )
}
