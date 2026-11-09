package com.antuan.the_player.domain.repository

import com.antuan.the_player.domain.model.MusicFolder
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a playlists. Vive en la capa de dominio: define QUÉ se
 * puede hacer con las playlists, sin saber CÓMO (Room, MediaStore...). La
 * implementación concreta está en la capa de datos.
 */
interface PlaylistRepository {

    /** Todas las canciones locales del dispositivo (desde el MediaStore). */
    suspend fun getAllSongs(): List<Song>

    /** Canciones agrupadas por carpeta física de almacenamiento. */
    suspend fun getFolders(): List<MusicFolder>

    /** Observa todas las playlists con sus canciones ya resueltas. */
    fun observePlaylists(): Flow<List<Playlist>>

    /** Observa una playlist concreta con sus canciones. */
    fun observePlaylist(playlistId: Long): Flow<Playlist?>

    /**
     * Lectura puntual de varias playlists por id, con sus canciones resueltas
     * desde el MediaStore. Pensada para construir la cola fusionada.
     */
    suspend fun getPlaylists(playlistIds: List<Long>): List<Playlist>

    /** Crea una playlist y devuelve su id. */
    suspend fun createPlaylist(name: String): Long

    /** Renombra una playlist existente sin tocar sus canciones. */
    suspend fun renamePlaylist(playlistId: Long, name: String)

    /** Elimina una playlist y sus referencias a canciones. */
    suspend fun deletePlaylist(playlistId: Long)

    /** Añade una canción al final de una playlist. */
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)

    /** Quita una canción de una playlist. */
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
}
