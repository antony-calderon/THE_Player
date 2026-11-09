package com.antuan.the_player.player

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Servicio de reproducción en segundo plano.
 *
 * Es un [MediaSessionService] de Media3: aloja un [ExoPlayer] dentro de una
 * [MediaSession], lo que da "gratis" la notificación multimedia del sistema,
 * los controles del bluetooth / auriculares / pantalla de bloqueo y la
 * continuidad de la reproducción con la app en segundo plano.
 *
 * El controlador de la UI (un MediaController) se conecta a este servicio a
 * través de la sesión; por eso aquí no hay lógica de negocio, solo el ciclo de
 * vida del reproductor y la sesión.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                // Manejo automático del foco de audio: pausa ante llamadas, etc.
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            // Pausa (en vez de seguir sonando por el altavoz) al desconectar auriculares.
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, player).build()
    }

    /**
     * El sistema (y los MediaController de la UI) piden la sesión por aquí.
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    /**
     * Si el usuario descarta la tarea y no hay nada reproduciéndose, paramos el
     * servicio para no dejar una notificación "colgada" ni consumir recursos.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
