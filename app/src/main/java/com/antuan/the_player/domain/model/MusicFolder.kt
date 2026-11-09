package com.antuan.the_player.domain.model

/**
 * Carpeta física del almacenamiento que contiene canciones, derivada de la
 * ruta de cada archivo en el MediaStore.
 */
data class MusicFolder(
    val path: String,
    val name: String,
    val songs: List<Song>
) {
    val songCount: Int get() = songs.size
}
