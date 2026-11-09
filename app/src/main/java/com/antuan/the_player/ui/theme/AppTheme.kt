package com.antuan.the_player.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.antuan.the_player.domain.model.ThemeMode

/**
 * Tema Material 3 de la app. Respeta el modo (sistema/claro/oscuro) y, si está
 * activado y el dispositivo lo soporta (Android 12+), el color dinámico
 * (Material You). Si no, cae a esquemas estáticos por defecto.
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        // El color dinámico (Material You) tiene prioridad si el usuario lo activa.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Sin color dinámico y en oscuro: paleta Neon Glassmorphism.
        darkTheme -> NeonDarkColorScheme
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
