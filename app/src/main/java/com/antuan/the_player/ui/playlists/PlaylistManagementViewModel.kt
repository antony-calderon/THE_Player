package com.antuan.the_player.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.domain.model.Song
import com.antuan.the_player.domain.repository.PlaylistRepository
import com.antuan.the_player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistManagementUiState(
    val playlists: List<Playlist> = emptyList(),
    val allSongs: List<Song> = emptyList(),
    val editing: Playlist? = null,
    val expandedId: Long? = null,
    val isLoadingSongs: Boolean = false
)

@HiltViewModel
class PlaylistManagementViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val editingId = MutableStateFlow<Long?>(null)
    private val expandedId = MutableStateFlow<Long?>(null)
    private val allSongs = MutableStateFlow<List<Song>>(emptyList())
    private val isLoadingSongs = MutableStateFlow(false)

    val uiState: StateFlow<PlaylistManagementUiState> =
        combine(
            playlistRepository.observePlaylists(),
            editingId,
            expandedId,
            allSongs,
            isLoadingSongs
        ) { playlists, editId, expId, songs, loading ->
            PlaylistManagementUiState(
                playlists = playlists,
                allSongs = songs,
                editing = playlists.find { it.id == editId },
                expandedId = expId,
                isLoadingSongs = loading
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaylistManagementUiState()
        )

    init {
        playerController.connect()
    }

    /** Reproduce una playlist individual al instante. */
    fun playPlaylist(playlist: Playlist) {
        if (playlist.songs.isEmpty()) return
        playerController.playSongs(playlist.songs, 0)
    }

    /** Expande/colapsa el detalle con las canciones de una playlist. */
    fun toggleExpanded(playlistId: Long) {
        expandedId.value = if (expandedId.value == playlistId) null else playlistId
    }

    fun createPlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { playlistRepository.createPlaylist(trimmed) }
    }

    fun renamePlaylist(playlistId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { playlistRepository.renamePlaylist(playlistId, trimmed) }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlistId) }
    }

    /** Abre el editor de una playlist y carga el catálogo de canciones. */
    fun openEditor(playlistId: Long) {
        editingId.value = playlistId
        if (allSongs.value.isEmpty()) {
            viewModelScope.launch {
                isLoadingSongs.value = true
                allSongs.value = playlistRepository.getAllSongs()
                isLoadingSongs.value = false
            }
        }
    }

    fun closeEditor() {
        editingId.value = null
    }

    fun addSong(playlistId: Long, songId: Long) {
        viewModelScope.launch { playlistRepository.addSongToPlaylist(playlistId, songId) }
    }

    fun removeSong(playlistId: Long, songId: Long) {
        viewModelScope.launch { playlistRepository.removeSongFromPlaylist(playlistId, songId) }
    }
}
