package com.antuan.the_player.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla de playlists. Solo guarda metadatos propios de la app; las canciones
 * no se duplican aquí: se referencian por su id de MediaStore a través de
 * [PlaylistSongCrossRef].
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
