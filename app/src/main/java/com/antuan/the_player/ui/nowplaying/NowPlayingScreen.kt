package com.antuan.the_player.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.player.PlaybackUiState
import com.antuan.the_player.player.RepeatMode
import com.antuan.the_player.ui.components.albumArtUri
import com.antuan.the_player.ui.components.neonBorder
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onEditSong: (Long) -> Unit = {},
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playback = state.playback
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    // La fuente de la carátula: portada custom si existe, si no la del álbum.
    val artworkModel: Any? = state.artworkModel ?: playback.albumId?.let { albumArtUri(it) }

    // Color dominante extraído con Palette -> fondo glass dinámico.
    val fallback = MaterialTheme.colorScheme.surface
    val dominant by rememberDominantColor(artworkModel, fallback)
    val animatedColor by animateColorAsState(targetValue = dominant, label = "glassColor")

    // Degradado translúcido que mezcla el color dominante con la superficie.
    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            animatedColor.copy(alpha = 0.55f).compositeOver(fallback),
            fallback
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(glassBrush),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Reproduciendo") },
                actions = {
                    val id = playback.currentSongId
                    if (id != null) {
                        IconButton(onClick = { onEditSong(id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar portada y letras")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (!playback.hasMedia) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay nada reproduciéndose.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Artwork(model = artworkModel)
            Spacer(Modifier.height(16.dp))

            Text(
                text = playback.title.ifEmpty { "Desconocido" },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = playback.artist,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))
            ProgressBar(playback = playback, onSeek = viewModel::seekTo)

            Spacer(Modifier.height(8.dp))
            SecondaryControls(
                playback = playback,
                onShuffle = viewModel::toggleShuffle,
                onRepeat = viewModel::cycleRepeatMode,
                onAddToPlaylist = viewModel::openAddToPlaylist
            )

            Spacer(Modifier.height(8.dp))
            MainControls(
                isPlaying = playback.isPlaying,
                onPrevious = viewModel::previous,
                onPlayPause = viewModel::playPause,
                onNext = viewModel::next
            )

            Spacer(Modifier.height(16.dp))
            LyricsTab(lyrics = state.lyrics)
        }
    }

    if (state.showAddToPlaylist) {
        AddToPlaylistDialog(
            playlists = state.playlists,
            onSelect = viewModel::addCurrentSongTo,
            onDismiss = viewModel::dismissAddToPlaylist
        )
    }
}

@Composable
private fun Artwork(model: Any?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f)
    ) {
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Carátula del álbum",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(playback: PlaybackUiState, onSeek: (Long) -> Unit) {
    var dragging by remember { mutableFloatStateOf(-1f) }
    val value = if (dragging >= 0f) dragging else playback.progress

    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = value,
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                if (playback.durationMs > 0) {
                    onSeek((dragging * playback.durationMs).toLong())
                }
                dragging = -1f
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(playback.positionMs), style = MaterialTheme.typography.labelSmall)
            Text(formatTime(playback.durationMs), style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Fila de controles avanzados: shuffle, repeat y añadir a playlist. */
@Composable
private fun SecondaryControls(
    playback: PlaybackUiState,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffle) {
            Icon(
                Icons.Filled.Shuffle,
                contentDescription = "Aleatorio",
                tint = if (playback.shuffleEnabled) active else inactive
            )
        }
        IconButton(onClick = onRepeat) {
            Icon(
                imageVector = if (playback.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne
                else Icons.Filled.Repeat,
                contentDescription = when (playback.repeatMode) {
                    RepeatMode.OFF -> "Repetición desactivada"
                    RepeatMode.ALL -> "Repetir todo"
                    RepeatMode.ONE -> "Repetir una"
                },
                tint = if (playback.repeatMode == RepeatMode.OFF) inactive else active
            )
        }
        IconButton(onClick = onAddToPlaylist) {
            Icon(
                Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = "Añadir a lista de reproducción",
                tint = inactive
            )
        }
    }
}

@Composable
private fun MainControls(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.size(16.dp))
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(72.dp).neonBorder(cornerRadius = 36.dp, alpha = 0.9f, width = 2.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.size(16.dp))
        IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsTab(lyrics: String) {
    var tab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Letras") })
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = lyrics.ifEmpty { "Esta canción no tiene letras guardadas. Añádelas desde el editor." },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a lista") },
        text = {
            if (playlists.isEmpty()) {
                Text("No tienes playlists. Crea una primero en la sección Playlists.")
            } else {
                Column {
                    playlists.forEach { playlist ->
                        ListItem(
                            modifier = Modifier.clickable { onSelect(playlist.id) },
                            headlineContent = { Text(playlist.name) },
                            supportingContent = { Text("${playlist.songCount} canciones") }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

private fun formatTime(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0))
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
