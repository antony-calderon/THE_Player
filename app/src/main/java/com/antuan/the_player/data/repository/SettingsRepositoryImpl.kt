package com.antuan.the_player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.antuan.the_player.domain.model.AppSettings
import com.antuan.the_player.domain.model.ThemeMode
import com.antuan.the_player.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore a nivel de archivo, asociado al Context de aplicación.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val REMOVE_DUPLICATES = booleanPreferencesKey("remove_duplicates_on_merge")
        val BACKGROUND_URI = stringPreferencesKey("background_uri")
        val BACKGROUND_OPACITY = floatPreferencesKey("background_opacity")
    }

    override val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            removeDuplicatesOnMerge = prefs[Keys.REMOVE_DUPLICATES] ?: true,
            backgroundUri = prefs[Keys.BACKGROUND_URI],
            backgroundOpacity = prefs[Keys.BACKGROUND_OPACITY] ?: 0.3f
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setRemoveDuplicatesOnMerge(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMOVE_DUPLICATES] = enabled }
    }

    override suspend fun setBackgroundUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(Keys.BACKGROUND_URI)
            else prefs[Keys.BACKGROUND_URI] = uri
        }
    }

    override suspend fun setBackgroundOpacity(opacity: Float) {
        context.dataStore.edit { it[Keys.BACKGROUND_OPACITY] = opacity.coerceIn(0f, 1f) }
    }
}
