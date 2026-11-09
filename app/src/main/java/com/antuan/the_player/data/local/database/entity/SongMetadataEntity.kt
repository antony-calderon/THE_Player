package com.antuan.the_player.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadatos editables por el usuario para una canción.
 *
 * El MediaStore es de solo lectura para datos como la carátula o inexistente
 * para las letras, así que estos campos "propios de la app" se guardan aparte,
 * indexados por el [songId] del MediaStore. Una canción sin personalizar
 * simplemente no tiene fila aquí.
 */
@Entity(tableName = "song_metadata")
data class SongMetadataEntity(
    @PrimaryKey
    val songId: Long,
    /** URI (content:// o file://) de una portada personalizada, si la hay. */
    val customArtworkUri: String? = null,
    /** Letra de la canción introducida por el usuario. */
    val lyrics: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
