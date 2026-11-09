package com.antuan.the_player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.domain.repository.PlaylistRepository
import com.antuan.the_player.domain.repository.SettingsRepository
import com.antuan.the_player.domain.usecase.MergePlaylistsShuffledUseCase
import com.antuan.the_player.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado observable de la pantalla de biblioteca. */
data class LibraryUiState(
    val playlists: List<Playlist> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isMerging: Boolean = false,
    val isSeeding: Boolean = false,
    val message: String? = null
) {
    val canMerge: Boolean get() = selectedIds.isNotEmpty() && !isMerging
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val mergePlaylistsShuffled: MergePlaylistsShuffledUseCase,
    private val settingsRepository: SettingsRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val transient = MutableStateFlow(TransientState())

    private data class TransientState(
        val isMerging: Boolean = false,
        val isSeeding: Boolean = false,
        val message: String? = null
    )

    val uiState: StateFlow<LibraryUiState> =
        combine(
            playlistRepository.observePlaylists(),
            selectedIds,
            transient
        ) { playlists, selected, t ->
            LibraryUiState(
                playlists = playlists,
                selectedIds = selected,
                isMerging = t.isMerging,
                isSeeding = t.isSeeding,
                message = t.message
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState()
        )

    init {
        // Conexión perezosa al servicio para tener el controlador listo.
        playerController.connect()
    }

    fun toggleSelection(playlistId: Long) {
        selectedIds.value = selectedIds.value.toMutableSet().apply {
            if (!add(playlistId)) remove(playlistId)
        }
    }

    /** Fusiona las playlists seleccionadas en una cola aleatoria y la reproduce. */
    fun mergeAndPlay() {
        val ids = selectedIds.value.toList()
        if (ids.isEmpty()) return

        viewModelScope.launch {
            transient.value = transient.value.copy(isMerging = true, message = null)
            try {
                // Respetamos la preferencia del usuario guardada en ajustes.
                val removeDuplicates = settingsRepository.settings.first().removeDuplicatesOnMerge
                val queue = mergePlaylistsShuffled(
                    playlistIds = ids,
                    removeDuplicates = removeDuplicates
                )
                if (queue.isEmpty) {
                    transient.value = transient.value.copy(
                        isMerging = false,
                        message = "Las playlists seleccionadas no tienen canciones."
                    )
                } else {
                    playerController.playQueue(queue)
                    transient.value = transient.value.copy(
                        isMerging = false,
                        message = "Reproduciendo ${queue.size} canciones fusionadas."
                    )
                }
            } catch (e: Exception) {
                transient.value = transient.value.copy(
                    isMerging = false,
                    message = "Error al fusionar: ${e.message}"
                )
            }
        }
    }

    fun playPause() = playerController.playPause()
    fun next() = playerController.next()

    fun consumeMessage() {
        transient.value = transient.value.copy(message = null)
    }

    /**
     * Crea dos playlists de ejemplo con canciones reales del dispositivo.
     *
     * Es una ayuda para poder probar el flujo completo en el emulador sin tener
     * aún una pantalla de gestión de playlists. No hace nada si el dispositivo
     * no tiene canciones.
     */
    fun seedSampleData() {
        viewModelScope.launch {
            transient.value = transient.value.copy(isSeeding = true, message = null)
            try {
                val songs = playlistRepository.getAllSongs()
                if (songs.isEmpty()) {
                    transient.value = transient.value.copy(
                        isSeeding = false,
                        message = "No se encontraron canciones en el dispositivo."
                    )
                    return@launch
                }

                val half = (songs.size + 1) / 2
                val first = songs.take(half)
                val second = songs.drop(half).ifEmpty { songs.take(half) }

                val id1 = playlistRepository.createPlaylist("Ejemplo A")
                first.forEach { playlistRepository.addSongToPlaylist(id1, it.id) }

                val id2 = playlistRepository.createPlaylist("Ejemplo B")
                second.forEach { playlistRepository.addSongToPlaylist(id2, it.id) }

                transient.value = transient.value.copy(
                    isSeeding = false,
                    message = "Creadas 2 playlists de ejemplo."
                )
            } catch (e: Exception) {
                transient.value = transient.value.copy(
                    isSeeding = false,
                    message = "Error al crear datos: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }
}
