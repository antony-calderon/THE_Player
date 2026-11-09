package com.antuan.the_player.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Tabla de cruce que modela la relación N:M entre playlists y canciones.
 *
 * No almacenamos los datos de la canción (título, artista...), solo su
 * [songId] del MediaStore, porque la fuente de verdad de las canciones es el
 * sistema. [position] permite conservar el orden en que el usuario las añadió.
 *
 * La clave primaria compuesta (playlistId + songId) evita duplicar la misma
 * canción dentro de una playlist. El borrado en cascada limpia las referencias
 * cuando se elimina la playlist.
 */
@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val position: Int = 0
)
