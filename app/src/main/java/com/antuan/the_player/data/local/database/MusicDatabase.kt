package com.antuan.the_player.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.antuan.the_player.data.local.database.dao.PlaylistDao
import com.antuan.the_player.data.local.database.dao.SongMetadataDao
import com.antuan.the_player.data.local.database.entity.PlaylistEntity
import com.antuan.the_player.data.local.database.entity.PlaylistSongCrossRef
import com.antuan.the_player.data.local.database.entity.SongMetadataEntity

/**
 * Base de datos Room de la app.
 *
 * Persiste datos propios: las playlists, la relación N:M con las canciones (por
 * id de MediaStore) y los metadatos editables (portada custom y letras). Las
 * canciones en sí NO se almacenan aquí; se leen del MediaStore del sistema.
 *
 * v2: se añadió [SongMetadataEntity] para portadas y letras editables.
 */
@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SongMetadataEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao

    abstract fun songMetadataDao(): SongMetadataDao

    companion object {
        const val DATABASE_NAME = "music.db"
    }
}
