package com.antuan.the_player.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.glance.appwidget.updateAll
import com.antuan.the_player.domain.model.MergedQueue
import com.antuan.the_player.domain.model.Song
import com.antuan.the_player.widget.MusicWidget
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada entre la app y el [PlaybackService].
 *
 * Se conecta a la [androidx.media3.session.MediaSession] del servicio mediante
 * un [MediaController] y traduce el modelo de dominio ([MergedQueue] / [Song])
 * a [MediaItem] de Media3. La UI y los ViewModels hablan con esta clase, no con
 * el servicio directamente.
 *
 * La conexión es perezosa y asíncrona: el [MediaController] se obtiene a través
 * de un [ListenableFuture] y las acciones se encolan hasta que esté listo.
 */
@Singleton
class PlayerController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val stateHolder: PlaybackStateHolder
) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val scope = CoroutineScope(Dispatchers.Main)
    private var positionJob: Job? = null

    // Huella del último estado empujado al widget, para refrescarlo solo cuando
    // cambia lo que realmente muestra (canción / play-pausa), no en cada tick
    // del bucle de posición.
    private var lastWidgetKey: String? = null

    private val _playbackState = MutableStateFlow(PlaybackUiState())

    /** Estado de reproducción en vivo para la UI (Now Playing, mini-player). */
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    /** true si ya hay un controlador conectado y operativo. */
    val isConnected: Boolean get() = controller?.isConnected == true

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            // Cualquier cambio relevante refresca el snapshot y el bucle de posición.
            refreshState()
            if (player.isPlaying) startPositionUpdates() else stopPositionUpdates()
        }
    }

    /**
     * Inicia (si hace falta) la conexión con el servicio. Es idempotente:
     * llamarla varias veces no abre conexiones duplicadas.
     */
    fun connect(onConnected: (() -> Unit)? = null) {
        if (controller != null || controllerFuture != null) {
            onConnected?.invoke()
            return
        }
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                val c = future.get()
                controller = c
                c.addListener(playerListener)
                refreshState()
                onConnected?.invoke()
            },
            MoreExecutors.directExecutor()
        )
    }

    /**
     * Carga la cola fusionada en el reproductor y empieza a reproducir.
     *
     * @param queue La cola producida por MergePlaylistsShuffledUseCase.
     * @param startIndex Índice inicial dentro de la cola.
     */
    fun playQueue(queue: MergedQueue, startIndex: Int = 0) {
        if (queue.isEmpty) return
        val items = queue.songs.map { it.toMediaItem() }
        withController { c ->
            c.setMediaItems(items, startIndex, /* startPositionMs = */ 0L)
            c.prepare()
            c.play()
        }
    }

    /**
     * Reproduce una lista arbitraria de canciones (p. ej. toda la biblioteca,
     * una carpeta, o una sola canción) empezando en [startIndex].
     */
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val items = songs.map { it.toMediaItem() }
        withController { c ->
            c.setMediaItems(items, startIndex.coerceIn(0, items.lastIndex), 0L)
            c.prepare()
            c.play()
        }
    }

    fun playPause() = withController { c ->
        if (c.isPlaying) c.pause() else c.play()
    }

    /** Alterna el modo aleatorio. */
    fun toggleShuffle() = withController { c ->
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    /** Cicla el modo de repetición: OFF -> ALL -> ONE -> OFF. */
    fun cycleRepeatMode() = withController { c ->
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun next() = withController(MediaController::seekToNextMediaItem)

    fun previous() = withController(MediaController::seekToPreviousMediaItem)

    fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs) }

    fun stop() = withController(Player::stop)

    /** Libera el controlador. Llamar cuando ya no se necesite (p. ej. en onCleared). */
    fun release() {
        stopPositionUpdates()
        controller?.removeListener(playerListener)
        controllerFuture?.let(MediaController::releaseFuture)
        controller = null
        controllerFuture = null
    }

    /** Vuelca el estado actual del reproductor al StateFlow (y al holder compartido). */
    private fun refreshState() {
        val c = controller
        if (c == null) {
            _playbackState.value = PlaybackUiState()
            stateHolder.update(_playbackState.value)
            pushWidgetIfChanged(_playbackState.value)
            return
        }
        val meta = c.mediaMetadata
        val mediaId = c.currentMediaItem?.mediaId?.toLongOrNull()
        _playbackState.value = PlaybackUiState(
            isPlaying = c.isPlaying,
            currentSongId = mediaId,
            albumId = meta.extras?.getLong(EXTRA_ALBUM_ID)?.takeIf { it > 0 },
            title = meta.title?.toString().orEmpty(),
            artist = meta.artist?.toString().orEmpty(),
            album = meta.albumTitle?.toString().orEmpty(),
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = c.duration.coerceAtLeast(0L),
            hasMedia = c.mediaItemCount > 0,
            shuffleEnabled = c.shuffleModeEnabled,
            repeatMode = when (c.repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
        )
        stateHolder.update(_playbackState.value)
        pushWidgetIfChanged(_playbackState.value)
    }

    /**
     * Empuja una actualización al widget solo cuando cambia lo que este muestra
     * (canción actual o estado play/pausa). Evita refrescar el widget cada
     * 500 ms por el mero avance de la posición.
     */
    private fun pushWidgetIfChanged(state: PlaybackUiState) {
        val key = "${state.currentSongId}|${state.isPlaying}|${state.title}|${state.artist}"
        if (key == lastWidgetKey) return
        lastWidgetKey = key
        scope.launch {
            runCatching { MusicWidget().updateAll(context) }
        }
    }

    /** Bucle que actualiza la posición mientras algo está sonando. */
    private fun startPositionUpdates() {
        if (positionJob?.isActive == true) return
        positionJob = scope.launch {
            while (true) {
                refreshState()
                delay(500L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    /**
     * Ejecuta [action] sobre el controlador si ya está conectado; si todavía no,
     * dispara la conexión y ejecuta la acción al terminar. Así la UI puede llamar
     * a playQueue() sin preocuparse del estado de la conexión.
     */
    private inline fun withController(crossinline action: (MediaController) -> Unit) {
        val current = controller
        if (current != null && current.isConnected) {
            action(current)
        } else {
            connect { controller?.let(action) }
        }
    }

    /**
     * Convierte un [Song] del dominio en un [MediaItem] de Media3, preservando
     * los metadatos para que la notificación y los controles los muestren.
     */
    private fun Song.toMediaItem(): MediaItem {
        // Guardamos el albumId en los extras para poder resolver la carátula
        // del álbum (content://.../albumart/{albumId}) desde el estado del player.
        val extras = android.os.Bundle().apply { putLong(EXTRA_ALBUM_ID, albumId) }
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setExtras(extras)
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }

    private companion object {
        const val EXTRA_ALBUM_ID = "albumId"
    }
}
