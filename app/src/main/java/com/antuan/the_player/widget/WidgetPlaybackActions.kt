package com.antuan.the_player.widget

import android.content.ComponentName
import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.antuan.the_player.player.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Conecta un [MediaController] efímero a la sesión del [PlaybackService],
 * ejecuta [action], da un instante para que el player aplique el cambio y
 * libera el controlador. Pensado para los botones del widget, que no comparten
 * el controlador de larga vida de la app.
 */
private suspend fun withEphemeralController(
    context: Context,
    action: (MediaController) -> Unit
) {
    val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
    val future = MediaController.Builder(context, token).buildAsync()
    val controller = suspendCoroutine<MediaController?> { cont ->
        future.addListener(
            { cont.resume(runCatching { future.get() }.getOrNull()) },
            MoreExecutors.directExecutor()
        )
    }
    controller?.let {
        action(it)
        // Breve margen para que el estado (play/pausa, pista) se asiente antes
        // de redibujar el widget con updateAll.
        delay(150L)
        it.release()
    }
}

/**
 * Ejecuta una acción de reproducción desde el widget y fuerza el redibujo de
 * todas las instancias del widget para reflejar el nuevo estado al instante.
 */
private suspend fun runAndRefresh(context: Context, action: (MediaController) -> Unit) {
    withEphemeralController(context, action)
    MusicWidget().updateAll(context)
}

/** Alterna play/pausa desde el widget. */
class WidgetPlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runAndRefresh(context) { c -> if (c.isPlaying) c.pause() else c.play() }
    }
}

/** Canción anterior desde el widget. */
class WidgetPreviousAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runAndRefresh(context) { c -> c.seekToPreviousMediaItem() }
    }
}

/** Siguiente canción desde el widget. */
class WidgetNextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runAndRefresh(context) { c -> c.seekToNextMediaItem() }
    }
}
