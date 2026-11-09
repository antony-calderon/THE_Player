package com.antuan.the_player.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta "Neon Glassmorphism": fondos muy oscuros y acentos vibrantes
 * (cian / magenta) que brillan sobre superficies translúcidas.
 */
object Neon {
    val Cyan = Color(0xFF00E5FF)
    val Magenta = Color(0xFFFF2DF7)
    val Purple = Color(0xFF7C4DFF)

    // Fondos y superficies oscuras.
    val Background = Color(0xFF05060A)
    val Surface = Color(0xFF0C0E16)
    val SurfaceVariant = Color(0xFF141826)

    // Color base translúcido para el "cristal" de tarjetas y barras.
    val Glass = Color(0x33141826)
    val GlassStrong = Color(0xAA0C0E16)

    val OnDark = Color(0xFFEAF6FF)
    val OnDarkMuted = Color(0xFF8CA0B3)
}

/** Esquema de color Material 3 oscuro con acentos neón. */
val NeonDarkColorScheme = darkColorScheme(
    primary = Neon.Cyan,
    onPrimary = Color(0xFF00242B),
    secondary = Neon.Magenta,
    onSecondary = Color(0xFF2B0028),
    tertiary = Neon.Purple,
    background = Neon.Background,
    onBackground = Neon.OnDark,
    surface = Neon.Surface,
    onSurface = Neon.OnDark,
    surfaceVariant = Neon.SurfaceVariant,
    onSurfaceVariant = Neon.OnDarkMuted,
    outline = Neon.Cyan.copy(alpha = 0.5f)
)
