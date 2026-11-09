package com.antuan.the_player.ui.components

import androidx.lifecycle.ViewModel
import com.antuan.the_player.player.PlaybackUiState
import com.antuan.the_player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel ligero para el mini-player. Solo expone el estado de reproducción
 * en vivo y el control de play/pausa; el resto de controles viven en Now Playing.
 */
@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    val playbackState: StateFlow<PlaybackUiState> = playerController.playbackState

    init {
        playerController.connect()
    }

    fun playPause() = playerController.playPause()
}
