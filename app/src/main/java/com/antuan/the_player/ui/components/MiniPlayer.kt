package com.antuan.the_player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Barra de reproducción persistente que se ubica justo encima de la barra de
 * navegación inferior. Aparece de forma animada solo cuando hay una canción
 * cargada, muestra carátula/título/artista y un control rápido de play/pausa,
 * y navega a la pantalla completa al tocarse.
 *
 * @param onClick Navega a la NowPlayingScreen.
 */
@Composable
fun MiniPlayer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = hiltViewModel()
) {
    val playback by viewModel.playbackState.collectAsStateWithLifecycle()

    // Solo visible cuando hay algo cargado en el reproductor.
    AnimatedVisibility(
        visible = playback.hasMedia,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .glassBar()
                .neonBorder(cornerRadius = 0.dp, alpha = 0.4f)
                .clickable(onClick = onClick)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlbumArt(
                        model = playback.albumId?.let { albumArtUri(it) },
                        modifier = Modifier.size(44.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = playback.title.ifEmpty { "Desconocido" },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = playback.artist,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = viewModel::playPause) {
                        Icon(
                            imageVector = if (playback.isPlaying) Icons.Filled.Pause
                            else Icons.Filled.PlayArrow,
                            contentDescription = if (playback.isPlaying) "Pausar" else "Reproducir"
                        )
                    }
                }
                // Fina barra de progreso de la canción actual.
                LinearProgressIndicator(
                    progress = { playback.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
