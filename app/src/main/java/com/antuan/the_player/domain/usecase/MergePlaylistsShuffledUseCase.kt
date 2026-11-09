package com.antuan.the_player.domain.usecase

import com.antuan.the_player.domain.model.MergedQueue
import com.antuan.the_player.domain.repository.PlaylistRepository
import com.antuan.the_player.player.ShuffleQueueBuilder
import javax.inject.Inject

/**
 * Caso de uso estrella: fusiona varias playlists en una única cola aleatoria.
 *
 * Conecta la capa de datos (vía [PlaylistRepository], que resuelve las
 * canciones desde Room + MediaStore) con el [ShuffleQueueBuilder], de modo que
 * la UI solo tenga que pasar los ids seleccionados y recibir la cola lista.
 */
class MergePlaylistsShuffledUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val shuffleQueueBuilder: ShuffleQueueBuilder
) {

    /**
     * @param playlistIds Ids de las playlists seleccionadas por el usuario.
     * @param removeDuplicates Evita repetir una canción presente en varias listas.
     * @param strategy Estrategia de mezcla (global o intercalada).
     * @param seed Semilla opcional para reconstruir un orden anterior.
     * @return La cola fusionada, o una cola vacía si no hay canciones.
     */
    suspend operator fun invoke(
        playlistIds: List<Long>,
        removeDuplicates: Boolean = true,
        strategy: ShuffleQueueBuilder.Strategy = ShuffleQueueBuilder.Strategy.GLOBAL_SHUFFLE,
        seed: Long = System.currentTimeMillis()
    ): MergedQueue {
        require(playlistIds.isNotEmpty()) { "Debe seleccionarse al menos una playlist" }

        val playlists = playlistRepository.getPlaylists(playlistIds)

        return shuffleQueueBuilder.build(
            playlists = playlists,
            removeDuplicates = removeDuplicates,
            strategy = strategy,
            seed = seed
        )
    }
}
