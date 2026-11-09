package com.antuan.the_player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Rutas de navegación de la app. */
object Routes {
    const val LIBRARY = "library"
    const val SONGS = "songs"
    const val PLAYLISTS = "playlists"
    const val FOLDERS = "folders"
    const val SETTINGS = "settings"
    const val NOW_PLAYING = "now_playing"

    const val SONG_EDITOR_ARG_ID = "songId"
    const val SONG_EDITOR = "song_editor/{$SONG_EDITOR_ARG_ID}"

    fun songEditor(songId: Long) = "song_editor/$songId"
}

/**
 * Destinos que aparecen en la barra de navegación inferior.
 *
 * Material 3 recomienda entre 3 y 5 destinos; usamos 5. "Reproductor"
 * (Now Playing) y el editor son rutas accesibles por navegación, no pestañas.
 */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    LIBRARY(Routes.LIBRARY, "Fusión", Icons.Filled.LibraryMusic),
    SONGS(Routes.SONGS, "Canciones", Icons.Filled.MusicNote),
    PLAYLISTS(Routes.PLAYLISTS, "Playlists", Icons.AutoMirrored.Filled.QueueMusic),
    FOLDERS(Routes.FOLDERS, "Carpetas", Icons.Filled.Folder),
    SETTINGS(Routes.SETTINGS, "Ajustes", Icons.Filled.Settings)
}
