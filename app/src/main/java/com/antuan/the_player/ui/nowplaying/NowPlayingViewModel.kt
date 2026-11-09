package com.antuan.the_player.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.domain.model.SongMetadata
import com.antuan.the_player.domain.repository.PlaylistRepository
import com.antuan.the_player.domain.repository.SongMetadataRepository
import com.antuan.the_player.player.PlaybackUiState
import com.antuan.the_player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NowPlayingUiState(
    val playback: PlaybackUiState = PlaybackUiState(),
    val metadata: SongMetadata? = null,
    val playlists: List<Playlist> = emptyList(),
    val showAddToPlaylist: Boolean = false,
    val message: String? = null
) {
    val artworkModel: Any?
        get() = metadata?.customArtworkUri
    val lyrics: String get() = metadata?.lyrics?.takeIf { it.isNotBlank() } ?: ""
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val metadataRepository: SongMetadataRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val transient = MutableStateFlow(Transient())

    private data class Transient(
        val showAddToPlaylist: Boolean = false,
        val message: String? = null
    )

    // Cuando cambia la canción actual, seguimos los metadatos de esa canción.
    private val metadataFlow = playerController.playbackState
        .flatMapLatest { state ->
            val id = state.currentSongId
            if (id == null) flowOf(null) else metadataRepository.observeMetadata(id)
        }

    val uiState: StateFlow<NowPlayingUiState> =
        combine(
            playerController.playbackState,
            metadataFlow,
            playlistRepository.observePlaylists(),
            transient
        ) { playback, metadata, playlists, t ->
            NowPlayingUiState(
                playback = playback,
                metadata = metadata,
                playlists = playlists,
                showAddToPlaylist = t.showAddToPlaylist,
                message = t.message
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NowPlayingUiState()
        )

    init {
        playerController.connect()
    }

    fun playPause() = playerController.playPause()
    fun next() = playerController.next()
    fun previous() = playerController.previous()
    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)
    fun toggleShuffle() = playerController.toggleShuffle()
    fun cycleRepeatMode() = playerController.cycleRepeatMode()

    fun openAddToPlaylist() {
        transient.value = transient.value.copy(showAddToPlaylist = true)
    }

    fun dismissAddToPlaylist() {
        transient.value = transient.value.copy(showAddToPlaylist = false)
    }

    /** Añade la canción actual a la playlist elegida. */
    fun addCurrentSongTo(playlistId: Long) {
        val songId = uiState.value.playback.currentSongId
        if (songId == null) {
            transient.value = transient.value.copy(
                showAddToPlaylist = false,
                message = "No hay ninguna canción en reproducción."
            )
            return
        }
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
            transient.value = transient.value.copy(
                showAddToPlaylist = false,
                message = "Canción añadida a la lista."
            )
        }
    }

    fun consumeMessage() {
        transient.value = transient.value.copy(message = null)
    }
}
