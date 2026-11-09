package com.antuan.the_player.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antuan.the_player.domain.model.MusicFolder
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

data class FoldersUiState(
    val isLoading: Boolean = true,
    val folders: List<MusicFolder> = emptyList(),
    val openFolder: MusicFolder? = null
)

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val folders = MutableStateFlow<List<MusicFolder>>(emptyList())
    private val openPath = MutableStateFlow<String?>(null)
    private val loading = MutableStateFlow(true)

    val uiState: StateFlow<FoldersUiState> =
        combine(folders, openPath, loading) { list, path, isLoading ->
            FoldersUiState(
                isLoading = isLoading,
                folders = list,
                openFolder = list.find { it.path == path }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FoldersUiState()
        )

    init {
        playerController.connect()
        viewModelScope.launch {
            loading.value = true
            folders.value = playlistRepository.getFolders()
            loading.value = false
        }
    }

    fun openFolder(path: String) { openPath.value = path }
    fun closeFolder() { openPath.value = null }

    fun playSong(song: Song) {
        val folder = uiState.value.openFolder ?: return
        val index = folder.songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerController.playSongs(folder.songs, index)
    }

    fun playFolder(folder: MusicFolder) {
        playerController.playSongs(folder.songs, 0)
    }
}
