package com.lastwave.app.ui.theme

import androidx.compose.ui.graphics.Color

val VoidBackground = Color(0xFF05070B)
val SurfaceGlassHigh = Color(0xFF10141C)
val SurfaceGlassLow = Color(0xFF0A0D14)
val SubduedGrayText = Color(0xFF94A3B8)

val CinematicVoid = Color(0xFF020408)
val AmbientGlowCyan = Color(0xFF00F0FF)
val GlassSurfacePanel = Color(0xFF0D121F)
val BokehBlurOverlay = Color(0x66020408)

// Dual-Engine Theme Colors
enum class EngineTheme(val id: String, val title: String) {
    MATERIAL3_EXPRESSIVE("m3_expressive", "Material 3 Expressive"),
    IOS_LIQUID_GLASS("ios_liquid_glass", "iOS Liquid Glass")
}

object EnginePalettes {
    // 1. Material 3 Expressive Engine Tone
    val M3ExpressivePrimary = Color(0xFF7F52FF)
    val M3ExpressiveSecondary = Color(0xFFFF2E93)
    val M3ExpressiveContainer = Color(0xFF1E1435)

    // 2. iOS Liquid Glass Specular Engine Tone
    val LiquidGlassPrimary = Color(0xFF00E5FF)
    val LiquidGlassSecondary = Color(0xFF7F52FF)
    val LiquidGlassContainer = Color(0xFF0A1220)
    val LiquidGlassBorder = Color(0x3300E5FF)
}

val M3ExpressivePrimary = EnginePalettes.M3ExpressivePrimary
val M3ExpressiveSecondary = EnginePalettes.M3ExpressiveSecondary
val LiquidGlassPrimary = EnginePalettes.LiquidGlassPrimary
val LiquidGlassSecondary = EnginePalettes.LiquidGlassSecondary
val LiquidGlassBorder = EnginePalettes.LiquidGlassBorder

val NeonCyanPrimary = Color(0xFF00E5FF)
val GlassBorderStroke = Color(0x3300E5FF)
