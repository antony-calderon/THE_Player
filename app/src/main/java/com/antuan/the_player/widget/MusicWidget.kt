package com.antuan.the_player.widget

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.Image
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.antuan.the_player.MainActivity
import com.antuan.the_player.R
import com.antuan.the_player.player.PlaybackUiState
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/**
 * Widget de pantalla de inicio (Jetpack Glance). Muestra la canción actual
 * (carátula, título, artista) y controles de play/pausa y siguiente,
 * leyendo el estado del [com.antuan.the_player.player.PlaybackStateHolder].
 */
class MusicWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Acceso al estado compartido vía EntryPoint de Hilt.
        val holder = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .playbackStateHolder()

        val state = holder.state.first()
        val artwork = state.albumId?.let { loadAlbumArt(context, it) }

        provideContent {
            WidgetContent(state, artwork)
        }
    }

    @Composable
    private fun WidgetContent(state: PlaybackUiState, artwork: Bitmap?) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xCC0A0A12)) // Oscuro translúcido (glass).
                .cornerRadius(20.dp)
                .padding(12.dp)
                // Tocar cualquier zona vacía del widget abre la app.
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Carátula (o icono por defecto).
            if (artwork != null) {
                Image(
                    provider = ImageProvider(artwork),
                    contentDescription = "Carátula",
                    modifier = GlanceModifier.size(56.dp)
                )
            } else {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = GlanceModifier.size(56.dp)
                )
            }

            Spacer(GlanceModifier.width(12.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = state.title.ifEmpty { "Nada en reproducción" },
                    style = TextStyle(color = ColorProvider(Color.White)),
                    maxLines = 1
                )
                Text(
                    text = state.artist,
                    style = TextStyle(color = ColorProvider(Color(0xFFBBBBBB))),
                    maxLines = 1
                )
            }

            // Anterior.
            Image(
                provider = ImageProvider(android.R.drawable.ic_media_previous),
                contentDescription = "Anterior",
                modifier = GlanceModifier
                    .size(40.dp)
                    .clickable(actionRunCallback<WidgetPreviousAction>())
            )
            Spacer(GlanceModifier.width(4.dp))
            // Play / Pausa.
            Image(
                provider = ImageProvider(
                    if (state.isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                ),
                contentDescription = if (state.isPlaying) "Pausar" else "Reproducir",
                modifier = GlanceModifier
                    .size(40.dp)
                    .clickable(actionRunCallback<WidgetPlayPauseAction>())
            )
            Spacer(GlanceModifier.width(4.dp))
            // Siguiente.
            Image(
                provider = ImageProvider(android.R.drawable.ic_media_next),
                contentDescription = "Siguiente",
                modifier = GlanceModifier
                    .size(40.dp)
                    .clickable(actionRunCallback<WidgetNextAction>())
            )
        }
    }

    /** Carga síncrona del bitmap de la carátula del álbum desde el MediaStore. */
    private fun loadAlbumArt(context: Context, albumId: Long): Bitmap? {
        val uri: Uri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"), albumId
        )
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }
}
