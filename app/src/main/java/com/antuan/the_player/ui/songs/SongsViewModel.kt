package com.antuan.the_player.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class SongsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val allSongs = MutableStateFlow<List<Song>>(emptyList())
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(true)

    val uiState: StateFlow<SongsUiState> =
        combine(allSongs, query, loading) { songs, q, isLoading ->
            val filtered = if (q.isBlank()) songs else songs.filter { song ->
                song.title.contains(q, ignoreCase = true) ||
                    song.artist.contains(q, ignoreCase = true) ||
                    song.album.contains(q, ignoreCase = true)
            }
            SongsUiState(isLoading = isLoading, query = q, songs = filtered)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SongsUiState()
        )

    init {
        playerController.connect()
        viewModelScope.launch {
            loading.value = true
            allSongs.value = playlistRepository.getAllSongs()
            loading.value = false
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    /** Reproduce la lista filtrada empezando por la canción tocada. */
    fun playSong(song: Song) {
        val list = uiState.value.songs
        val index = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerController.playSongs(list, index)
    }
}
