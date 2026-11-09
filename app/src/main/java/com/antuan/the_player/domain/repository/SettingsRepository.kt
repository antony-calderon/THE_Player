package com.antuan.the_player.domain.repository

import com.antuan.the_player.domain.model.AppSettings
import com.antuan.the_player.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** Contrato para leer y persistir las preferencias de usuario. */
interface SettingsRepository {

    /** Flujo reactivo de las preferencias actuales. */
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setRemoveDuplicatesOnMerge(enabled: Boolean)
    suspend fun setBackgroundUri(uri: String?)
    suspend fun setBackgroundOpacity(opacity: Float)
}
