package com.antuan.the_player.data.local.database.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Resultado de lectura: una playlist junto con sus referencias a canciones.
 *
 * Room resuelve la relación automáticamente. Las referencias traen el [songId]
 * de MediaStore y la [position]; la capa de datos las cruza con el MediaStore
 * para reconstruir los objetos Song completos del dominio.
 */
data class PlaylistWithSongRefs(
    @Embedded
    val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val songRefs: List<PlaylistSongCrossRef>
)
