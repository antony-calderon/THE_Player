package com.antuan.the_player.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente compartida del estado de reproducción, accesible tanto desde la UI de
 * la app como desde el widget (Glance). El [PlayerController] la actualiza; el
 * widget solo la lee. Es un Singleton de Hilt para que ambos vean lo mismo.
 */
@Singleton
class PlaybackStateHolder @Inject constructor() {

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    fun update(state: PlaybackUiState) {
        _state.value = state
    }
}
