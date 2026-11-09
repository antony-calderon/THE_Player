package com.antuan.the_player.domain.model

/** Preferencia de tema de la interfaz. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Preferencias de usuario de la app.
 *
 * @property themeMode Claro / Oscuro / Según el sistema.
 * @property dynamicColor Usar color dinámico Material You (Android 12+).
 * @property removeDuplicatesOnMerge Preferencia por defecto para la fusión.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val removeDuplicatesOnMerge: Boolean = true,
    /** URI de una imagen local usada como fondo global, o null. */
    val backgroundUri: String? = null,
    /** Opacidad del fondo personalizado, 0f (invisible) .. 1f (opaco). */
    val backgroundOpacity: Float = 0.3f
)
