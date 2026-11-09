package com.antuan.the_player.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mensajes efímeros (resultado de fusionar, sembrar, errores...).
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { TopAppBar(title = { Text("Mi Reproductor") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (state.playlists.isEmpty()) {
                EmptyState(
                    isSeeding = state.isSeeding,
                    onSeed = viewModel::seedSampleData,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Selecciona playlists para fusionar",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.padding(4.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.playlists, key = { it.id }) { playlist ->
                        PlaylistRow(
                            playlist = playlist,
                            selected = playlist.id in state.selectedIds,
                            onToggle = { viewModel.toggleSelection(playlist.id) }
                        )
                    }
                }

                Spacer(Modifier.padding(8.dp))
                MergeButton(
                    canMerge = state.canMerge,
                    isMerging = state.isMerging,
                    onMergeAndPlay = viewModel::mergeAndPlay
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    selected: Boolean,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        highlighted = selected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
            Spacer(Modifier.padding(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${playlist.songCount} canciones",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MergeButton(
    canMerge: Boolean,
    isMerging: Boolean,
    onMergeAndPlay: () -> Unit
) {
    // Interfaz enfocada: solo la acción principal de fusionar. Los controles de
    // reproducción viven en el mini-player y en Now Playing.
    Button(
        onClick = onMergeAndPlay,
        enabled = canMerge,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isMerging) {
            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            Text("Fusionando...")
        } else {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text("  Fusionar y reproducir")
        }
    }
}

@Composable
private fun EmptyState(
    isSeeding: Boolean,
    onSeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay playlists todavía",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.padding(4.dp))
        Text(
            text = "Crea un par de playlists de ejemplo con las canciones de tu dispositivo para probar la fusión.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.padding(8.dp))
        OutlinedButton(onClick = onSeed, enabled = !isSeeding) {
            if (isSeeding) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                Text("Creando...")
            } else {
                Text("Crear playlists de ejemplo")
            }
        }
    }
}
