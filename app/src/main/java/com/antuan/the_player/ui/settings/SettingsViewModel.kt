package com.antuan.the_player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antuan.the_player.domain.model.AppSettings
import com.antuan.the_player.domain.model.ThemeMode
import com.antuan.the_player.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        settingsRepository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    fun setRemoveDuplicates(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRemoveDuplicatesOnMerge(enabled) }
    }

    fun setBackgroundUri(uri: String?) {
        viewModelScope.launch { settingsRepository.setBackgroundUri(uri) }
    }

    fun setBackgroundOpacity(opacity: Float) {
        viewModelScope.launch { settingsRepository.setBackgroundOpacity(opacity) }
    }
}
