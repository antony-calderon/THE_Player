package com.antuan.the_player.data.repository

import com.antuan.the_player.data.local.database.dao.PlaylistDao
import com.antuan.the_player.data.local.database.entity.PlaylistEntity
import com.antuan.the_player.data.local.database.entity.PlaylistSongCrossRef
import com.antuan.the_player.data.local.database.entity.PlaylistWithSongRefs
import com.antuan.the_player.data.local.mediastore.MediaStoreDataSource
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.domain.model.Song
import com.antuan.the_player.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de playlists.
 *
 * Combina las dos fuentes: Room guarda la estructura de la playlist (nombre,
 * orden, qué songId la componen) y el MediaStore aporta los datos reales de
 * cada canción. Aquí se cruzan ambos para reconstruir los [Playlist] del
 * dominio con sus [Song] completas y en el orden en que el usuario las añadió.
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val mediaStore: MediaStoreDataSource
) : PlaylistRepository {

    override suspend fun getAllSongs(): List<Song> = mediaStore.getAllSongs()

    override suspend fun getFolders(): List<com.antuan.the_player.domain.model.MusicFolder> =
        mediaStore.getFolders()

    override fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observePlaylistsWithSongs().map { list ->
            // Resolvemos todas las canciones de todas las playlists en una sola
            // consulta al MediaStore para no golpear el disco por cada lista.
            val allIds = list.flatMap { it.songRefs.map(PlaylistSongCrossRef::songId) }.distinct()
            val songsById = mediaStore.getSongsByIds(allIds).associateBy(Song::id)
            list.map { it.toDomain(songsById) }
        }

    override fun observePlaylist(playlistId: Long): Flow<Playlist?> =
        playlistDao.observePlaylistWithSongs(playlistId).map { relation ->
            relation?.let {
                val ids = it.songRefs.map(PlaylistSongCrossRef::songId)
                val songsById = mediaStore.getSongsByIds(ids).associateBy(Song::id)
                it.toDomain(songsById)
            }
        }

    override suspend fun getPlaylists(playlistIds: List<Long>): List<Playlist> {
        val relations = playlistDao.getPlaylistsWithSongs(playlistIds)
        val allIds = relations.flatMap { it.songRefs.map(PlaylistSongCrossRef::songId) }.distinct()
        val songsById = mediaStore.getSongsByIds(allIds).associateBy(Song::id)
        return relations.map { it.toDomain(songsById) }
    }

    override suspend fun createPlaylist(name: String): Long =
        playlistDao.upsertPlaylist(PlaylistEntity(name = name))

    override suspend fun renamePlaylist(playlistId: Long, name: String) =
        playlistDao.renamePlaylist(playlistId, name)

    override suspend fun deletePlaylist(playlistId: Long) =
        playlistDao.deletePlaylist(playlistId)

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val position = playlistDao.nextPositionFor(playlistId)
        playlistDao.insertSongRef(
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId, position = position)
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        playlistDao.removeSongFromPlaylist(playlistId, songId)

    /**
     * Convierte la relación de Room en un [Playlist] de dominio.
     *
     * Las canciones se ordenan por [PlaylistSongCrossRef.position] (el orden en
     * que el usuario las añadió) y se descartan las referencias cuyo archivo ya
     * no exista en el MediaStore (p. ej. el usuario borró el mp3).
     */
    private fun PlaylistWithSongRefs.toDomain(songsById: Map<Long, Song>): Playlist {
        val songs = songRefs
            .sortedBy { it.position }
            .mapNotNull { songsById[it.songId] }

        return Playlist(
            id = playlist.id,
            name = playlist.name,
            songs = songs,
            createdAt = playlist.createdAt
        )
    }
}
