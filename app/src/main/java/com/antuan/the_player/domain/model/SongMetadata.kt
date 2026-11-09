package com.antuan.the_player.domain.model

/**
 * Metadatos editables por el usuario para una canción (portada y letras).
 * Datos propios de la app, independientes del MediaStore.
 */
data class SongMetadata(
    val songId: Long,
    val customArtworkUri: String? = null,
    val lyrics: String? = null
)
