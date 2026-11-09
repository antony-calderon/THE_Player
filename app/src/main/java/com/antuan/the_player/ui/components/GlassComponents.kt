package com.antuan.the_player.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antuan.the_player.ui.theme.Neon

/**
 * Aplica un borde neón luminoso (degradado cian -> magenta) con esquinas
 * redondeadas. Base del lenguaje visual "glassmorphism" de la app.
 */
fun Modifier.neonBorder(
    cornerRadius: Dp = 16.dp,
    alpha: Float = 0.6f,
    width: Dp = 1.dp
): Modifier = this.border(
    border = BorderStroke(
        width = width,
        brush = Brush.linearGradient(
            listOf(
                Neon.Cyan.copy(alpha = alpha),
                Neon.Magenta.copy(alpha = alpha)
            )
        )
    ),
    shape = RoundedCornerShape(cornerRadius)
)

/**
 * Tarjeta con estilo cristal: fondo oscuro translúcido + borde neón sutil.
 * Reemplaza a Card en las listas y secciones para dar coherencia global.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    highlighted: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                if (highlighted) Neon.Cyan.copy(alpha = 0.12f) else Neon.Glass
            )
            .neonBorder(
                cornerRadius = cornerRadius,
                alpha = if (highlighted) 0.9f else 0.35f
            )
    ) {
        content()
    }
}

/** Fondo glass translúcido para barras (bottom bar, mini-player). */
fun Modifier.glassBar(): Modifier = this
    .background(
        Brush.verticalGradient(
            listOf(Color.Transparent, Neon.GlassStrong)
        )
    )
