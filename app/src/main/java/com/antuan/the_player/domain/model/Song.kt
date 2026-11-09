package com.antuan.the_player.domain.model

import android.net.Uri

/**
 * Representa una canción local del dispositivo.
 *
 * Los datos provienen del MediaStore de Android, por lo que [id] es el
 * identificador estable del sistema y [uri] es el content:// URI que se
 * entrega directamente a ExoPlayer para reproducir.
 */
data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val trackNumber: Int? = null,
    val year: Int? = null,
    val path: String,
    val sizeBytes: Long = 0L,
    val mimeType: String? = null
)
