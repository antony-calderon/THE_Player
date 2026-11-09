package com.antuan.the_player.ui.folders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antuan.the_player.domain.model.MusicFolder
import com.antuan.the_player.ui.components.AlbumArt
import com.antuan.the_player.ui.components.albumArtUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    viewModel: FoldersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val open = state.openFolder

    // Botón atrás del sistema cierra la carpeta abierta antes de salir.
    BackHandler(enabled = open != null) { viewModel.closeFolder() }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(open?.name ?: "Carpetas") },
                navigationIcon = {
                    if (open != null) {
                        IconButton(onClick = viewModel::closeFolder) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            open != null -> {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(open.songs, key = { it.id }) { song ->
                        ListItem(
                            modifier = Modifier.clickable { viewModel.playSong(song) },
                            leadingContent = {
                                AlbumArt(albumArtUri(song.albumId), Modifier.size(48.dp))
                            },
                            headlineContent = { Text(song.title, maxLines = 1) },
                            supportingContent = { Text(song.artist, maxLines = 1) }
                        )
                    }
                }
            }
            state.folders.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No se encontraron carpetas con música.")
                }
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(state.folders, key = { it.path }) { folder ->
                        FolderItem(
                            folder = folder,
                            onOpen = { viewModel.openFolder(folder.path) },
                            onPlay = { viewModel.playFolder(folder) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderItem(folder: MusicFolder, onOpen: () -> Unit, onPlay: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onOpen),
        leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
        headlineContent = { Text(folder.name, maxLines = 1) },
        supportingContent = { Text("${folder.songCount} canciones") },
        trailingContent = {
            IconButton(onClick = onPlay) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir carpeta")
            }
        }
    )
}
