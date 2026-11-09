package com.antuan.the_player.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.antuan.the_player.data.local.database.entity.PlaylistEntity
import com.antuan.the_player.data.local.database.entity.PlaylistSongCrossRef
import com.antuan.the_player.data.local.database.entity.PlaylistWithSongRefs
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de playlists y su relación con las canciones.
 *
 * Las lecturas devuelven [Flow] para que la UI reaccione de forma automática a
 * los cambios en la base de datos.
 */
@Dao
interface PlaylistDao {

    // ---------- Playlists ----------

    @Upsert
    suspend fun upsertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun observePlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongRefs?>

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylistsWithSongs(): Flow<List<PlaylistWithSongRefs>>

    /** Lectura puntual (no reactiva), útil para construir la cola fusionada. */
    @Transaction
    @Query("SELECT * FROM playlists WHERE id IN (:playlistIds)")
    suspend fun getPlaylistsWithSongs(playlistIds: List<Long>): List<PlaylistWithSongRefs>

    // ---------- Canciones dentro de una playlist ----------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongRef(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Query(
        "SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_song_cross_ref WHERE playlistId = :playlistId"
    )
    suspend fun nextPositionFor(playlistId: Long): Int
}
