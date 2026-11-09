package com.antuan.the_player.player

/** Modo de repetición del reproductor. */
enum class RepeatMode { OFF, ALL, ONE }

/**
 * Instantánea del estado de reproducción para la UI (Now Playing, mini-player).
 *
 * [currentSongId] es el media id (el id de MediaStore como String) del item en
 * reproducción, para poder cruzarlo con metadatos y carátulas.
 */
data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val currentSongId: Long? = null,
    val albumId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasMedia: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
) {
    /** Progreso 0f..1f, seguro ante duración desconocida. */
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}
