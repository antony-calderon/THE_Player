package com.antuan.the_player.player

import com.antuan.the_player.domain.model.MergedQueue
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.domain.model.Song
import javax.inject.Inject
import kotlin.random.Random

/**
 * Construye la cola aleatoria "inteligente" a partir de varias playlists.
 *
 * Es puro Kotlin (sin dependencias de Android) para que sea trivial de testear.
 * La aleatoriedad es *determinista*: dada la misma semilla y las mismas
 * playlists de entrada, produce exactamente el mismo orden. Eso permite
 * reconstruir una sesión "aleatoria" tras cerrar la app (ver [MergedQueue.seed]).
 */
class ShuffleQueueBuilder @Inject constructor() {

    /**
     * Estrategia para repartir las canciones de varias playlists en la cola.
     */
    enum class Strategy {
        /** Mezcla global: se juntan todas las canciones y se barajan por igual. */
        GLOBAL_SHUFFLE,

        /**
         * Reparto intercalado ("round-robin") y luego barajado dentro de cada
         * vuelta. Favorece que canciones de todas las playlists aparezcan pronto,
         * evitando que una playlist muy larga domine el principio de la cola.
         */
        INTERLEAVED
    }

    /**
     * @param playlists Playlists seleccionadas por el usuario.
     * @param removeDuplicates Si es true, una misma canción (por [Song.id]) aparece
     *   una sola vez aunque esté en varias playlists.
     * @param strategy Cómo combinar las canciones antes de barajar.
     * @param seed Semilla del aleatorio. Por defecto se genera una nueva; pásala
     *   explícita para reproducir un orden anterior.
     */
    fun build(
        playlists: List<Playlist>,
        removeDuplicates: Boolean = true,
        strategy: Strategy = Strategy.GLOBAL_SHUFFLE,
        seed: Long = System.currentTimeMillis()
    ): MergedQueue {
        val random = Random(seed)

        val combined = when (strategy) {
            Strategy.GLOBAL_SHUFFLE -> globalShuffle(playlists, removeDuplicates, random)
            Strategy.INTERLEAVED -> interleavedShuffle(playlists, removeDuplicates, random)
        }

        return MergedQueue(
            songs = combined,
            sourcePlaylistIds = playlists.map { it.id },
            seed = seed
        )
    }

    /** Junta todas las canciones y las baraja de una sola vez. */
    private fun globalShuffle(
        playlists: List<Playlist>,
        removeDuplicates: Boolean,
        random: Random
    ): List<Song> {
        val all = playlists.flatMap { it.songs }
        val deduped = if (removeDuplicates) all.distinctBy { it.id } else all
        return deduped.shuffled(random)
    }

    /**
     * Intercala las playlists por posición (una canción de cada una por vuelta),
     * barajando el orden de las playlists en cada vuelta para no favorecer siempre
     * a la misma. El resultado es variado pero con buena mezcla de orígenes.
     */
    private fun interleavedShuffle(
        playlists: List<Playlist>,
        removeDuplicates: Boolean,
        random: Random
    ): List<Song> {
        // Barajamos las canciones dentro de cada playlist primero.
        val buckets = playlists
            .map { it.songs.shuffled(random).toMutableList() }
            .filter { it.isNotEmpty() }
            .toMutableList()

        val result = ArrayList<Song>()
        val seen = HashSet<Long>()

        while (buckets.isNotEmpty()) {
            // En cada vuelta variamos el orden de las playlists.
            buckets.shuffle(random)
            val iterator = buckets.iterator()
            while (iterator.hasNext()) {
                val bucket = iterator.next()
                val song = bucket.removeAt(0)
                if (!removeDuplicates || seen.add(song.id)) {
                    result.add(song)
                }
                if (bucket.isEmpty()) iterator.remove()
            }
        }
        return result
    }
}
