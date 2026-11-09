package com.antuan.the_player.domain.model

/**
 * Lista de reproducción creada por el usuario.
 *
 * A diferencia de [Song] (que se lee del MediaStore del sistema), las
 * playlists son datos propios de la app y se persisten en Room.
 */
data class Playlist(
    val id: Long = 0L,
    val name: String,
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Número de canciones que contiene la lista. */
    val songCount: Int get() = songs.size

    /** Duración total de la lista en milisegundos. */
    val totalDurationMs: Long get() = songs.sumOf { it.durationMs }
}
