package com.antuan.the_player.domain.model

/**
 * Cola de reproducción resultante de fusionar varias playlists en una única
 * secuencia aleatoria. Es el producto de la característica estrella de la app
 * (ver MergePlaylistsShuffledUseCase y ShuffleQueueBuilder).
 *
 * @property songs Orden final (ya barajado) de las canciones.
 * @property sourcePlaylistIds Playlists de origen, para poder mostrar algo como
 *   "sonando desde: Rock + Study".
 * @property seed Semilla del aleatorio. Al persistirla, el mismo orden
 *   "aleatorio" puede reconstruirse de forma determinista en otra sesión.
 */
data class MergedQueue(
    val songs: List<Song>,
    val sourcePlaylistIds: List<Long>,
    val seed: Long = System.currentTimeMillis()
) {
    val size: Int get() = songs.size

    val isEmpty: Boolean get() = songs.isEmpty()
}

/** Estado del motor de reproducción (Media3 / ExoPlayer). */
enum class PlaybackState { IDLE, BUFFERING, PLAYING, PAUSED, ENDED }
