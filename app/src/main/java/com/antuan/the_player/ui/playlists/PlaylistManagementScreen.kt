package com.antuan.the_player.ui.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antuan.the_player.domain.model.Playlist
import com.antuan.the_player.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManagementScreen(
    onOpenNowPlaying: () -> Unit = {},
    viewModel: PlaylistManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editing = state.editing

    if (editing != null) {
        PlaylistEditor(
            playlist = editing,
            allSongs = state.allSongs,
            isLoading = state.isLoadingSongs,
            onBack = viewModel::closeEditor,
            onAdd = { viewModel.addSong(editing.id, it) },
            onRemove = { viewModel.removeSong(editing.id, it) }
        )
    } else {
        PlaylistList(
            playlists = state.playlists,
            expandedId = state.expandedId,
            onCreate = viewModel::createPlaylist,
            onDelete = viewModel::deletePlaylist,
            onEdit = viewModel::openEditor,
            onPlay = viewModel::playPlaylist,
            onToggleExpand = viewModel::toggleExpanded,
            onRename = viewModel::renamePlaylist
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistList(
    playlists: List<Playlist>,
    expandedId: Long?,
    onCreate: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onPlay: (Playlist) -> Unit,
    onToggleExpand: (Long) -> Unit,
    onRename: (Long, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    // Playlist en proceso de renombrado (null = diálogo cerrado).
    var renaming by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { TopAppBar(title = { Text("Mis Playlists") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Crear playlist")
            }
        }
    ) { padding ->
        if (playlists.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no hay playlists. Crea una con el botón +.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        expanded = expandedId == playlist.id,
                        onPlay = { onPlay(playlist) },
                        onEdit = { onEdit(playlist.id) },
                        onDelete = { onDelete(playlist.id) },
                        onToggleExpand = { onToggleExpand(playlist.id) },
                        onRename = { renaming = playlist }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        NamePlaylistDialog(
            title = "Nueva playlist",
            initialName = "",
            confirmLabel = "Crear",
            onConfirm = {
                onCreate(it)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    renaming?.let { playlist ->
        NamePlaylistDialog(
            title = "Renombrar playlist",
            initialName = playlist.name,
            confirmLabel = "Guardar",
            onConfirm = {
                onRename(playlist.id, it)
                renaming = null
            },
            onDismiss = { renaming = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    expanded: Boolean,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleExpand: () -> Unit,
    onRename: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), highlighted = expanded) {
        Column {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(playlist.name) },
                supportingContent = { Text("${playlist.songCount} canciones") },
                leadingContent = {
                    // Reproducción directa de esta lista.
                    IconButton(onClick = onPlay, enabled = playlist.songs.isNotEmpty()) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir lista")
                    }
                },
                trailingContent = {
                    Row {
                        IconButton(onClick = onToggleExpand) {
                            Icon(
                                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (expanded) "Ocultar canciones" else "Ver canciones"
                            )
                        }
                        IconButton(onClick = onRename) {
                            Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = "Renombrar")
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar canciones")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )

            // Detalle expandible con las canciones que contiene la playlist.
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    if (playlist.songs.isEmpty()) {
                        Text(
                            "Esta lista está vacía. Añade canciones desde el botón editar.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        playlist.songs.forEachIndexed { index, song ->
                            Text(
                                text = "${index + 1}. ${song.title} — ${song.artist}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NamePlaylistDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistEditor(
    playlist: Playlist,
    allSongs: List<com.antuan.the_player.domain.model.Song>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    val memberIds = remember(playlist) { playlist.songs.map { it.id }.toSet() }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Editar: ${playlist.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Cargando canciones del dispositivo...")
            }
        } else if (allSongs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontraron canciones en el dispositivo.")
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(
                    text = "Toca una canción para añadirla o quitarla de la playlist",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
                LazyColumn(Modifier.fillMaxSize()) {
                    items(allSongs, key = { it.id }) { song ->
                        val isMember = song.id in memberIds
                        ListItem(
                            headlineContent = { Text(song.title) },
                            supportingContent = { Text(song.artist) },
                            trailingContent = {
                                if (isMember) {
                                    Icon(Icons.Filled.Check, contentDescription = "En la playlist")
                                } else {
                                    Icon(Icons.Filled.Add, contentDescription = "Añadir")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().let { m ->
                                m
                            }
                        )
                        Spacer(Modifier.padding(0.dp))
                        // Interacción: toca para alternar pertenencia.
                        ToggleOverlay(
                            isMember = isMember,
                            onClick = { if (isMember) onRemove(song.id) else onAdd(song.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pequeño botón de texto que alterna la pertenencia. Se separa del ListItem
 * para mantener el click explícito y accesible.
 */
@Composable
private fun ToggleOverlay(isMember: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.padding(start = 12.dp)) {
        Text(if (isMember) "Quitar de la playlist" else "Añadir a la playlist")
    }
}
