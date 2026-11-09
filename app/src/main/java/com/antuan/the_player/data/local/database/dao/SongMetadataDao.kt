package com.antuan.the_player.data.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.antuan.the_player.data.local.database.entity.SongMetadataEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a los metadatos editables de las canciones (portada custom y letras).
 */
@Dao
interface SongMetadataDao {

    @Upsert
    suspend fun upsert(metadata: SongMetadataEntity)

    @Query("SELECT * FROM song_metadata WHERE songId = :songId")
    fun observe(songId: Long): Flow<SongMetadataEntity?>

    @Query("SELECT * FROM song_metadata WHERE songId = :songId")
    suspend fun get(songId: Long): SongMetadataEntity?

    @Query("SELECT * FROM song_metadata WHERE songId IN (:songIds)")
    suspend fun getByIds(songIds: List<Long>): List<SongMetadataEntity>

    @Query("DELETE FROM song_metadata WHERE songId = :songId")
    suspend fun delete(songId: Long)
}
