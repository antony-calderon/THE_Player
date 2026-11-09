package com.antuan.the_player.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antuan.the_player.domain.repository.SongMetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SongEditorUiState(
    val songId: Long? = null,
    val artworkUri: String? = null,
    val lyrics: String = "",
    val saved: Boolean = false
)

@HiltViewModel
class SongEditorViewModel @Inject constructor(
    private val metadataRepository: SongMetadataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongEditorUiState())
    val uiState: StateFlow<SongEditorUiState> = _uiState.asStateFlow()

    /** Carga los metadatos actuales de la canción a editar. */
    fun load(songId: Long) {
        _uiState.value = _uiState.value.copy(songId = songId)
        viewModelScope.launch {
            val meta = metadataRepository.getMetadata(songId)
            _uiState.value = _uiState.value.copy(
                artworkUri = meta?.customArtworkUri,
                lyrics = meta?.lyrics.orEmpty()
            )
        }
    }

    fun onArtworkPicked(uri: String) {
        _uiState.value = _uiState.value.copy(artworkUri = uri)
    }

    fun onLyricsChanged(value: String) {
        _uiState.value = _uiState.value.copy(lyrics = value)
    }

    /** Persiste portada y letras en Room. */
    fun save() {
        val id = _uiState.value.songId ?: return
        viewModelScope.launch {
            metadataRepository.setArtwork(id, _uiState.value.artworkUri)
            metadataRepository.setLyrics(id, _uiState.value.lyrics.takeIf { it.isNotBlank() })
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    fun consumeSaved() {
        _uiState.value = _uiState.value.copy(saved = false)
    }
}
