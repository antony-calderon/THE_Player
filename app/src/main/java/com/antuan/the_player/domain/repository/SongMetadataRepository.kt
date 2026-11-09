package com.antuan.the_player.domain.repository

import com.antuan.the_player.domain.model.SongMetadata
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para leer y guardar los metadatos editables de una canción
 * (portada personalizada y letras).
 */
interface SongMetadataRepository {

    /** Observa los metadatos de una canción (null si aún no tiene). */
    fun observeMetadata(songId: Long): Flow<SongMetadata?>

    /** Lectura puntual de los metadatos. */
    suspend fun getMetadata(songId: Long): SongMetadata?

    /** Crea o actualiza la portada personalizada. */
    suspend fun setArtwork(songId: Long, artworkUri: String?)

    /** Crea o actualiza la letra. */
    suspend fun setLyrics(songId: Long, lyrics: String?)
}
