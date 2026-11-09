package com.antuan.the_player.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.antuan.the_player.ui.components.MiniPlayer
import com.antuan.the_player.ui.components.glassBar
import com.antuan.the_player.ui.theme.Neon
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.antuan.the_player.ui.editor.SongEditorScreen
import com.antuan.the_player.ui.folders.FoldersScreen
import com.antuan.the_player.ui.library.LibraryScreen
import com.antuan.the_player.ui.nowplaying.NowPlayingScreen
import com.antuan.the_player.ui.playlists.PlaylistManagementScreen
import com.antuan.the_player.ui.settings.SettingsScreen
import com.antuan.the_player.ui.songs.SongsScreen

/**
 * Grafo de navegación principal. Un Scaffold con barra inferior para los tres
 * destinos de nivel superior (Biblioteca, Playlists, Reproductor) y una ruta de
 * detalle (editor) que oculta la barra.
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // La barra inferior solo se muestra en los destinos de nivel superior.
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        // Transparente para dejar ver el fondo personalizado de la app. El color
        // base lo aporta la capa raíz (MainActivity).
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    // El mini-player va justo encima de la barra de navegación y
                    // se muestra/oculta solo según haya reproducción activa.
                    MiniPlayer(
                        onClick = {
                            navController.navigate(Routes.NOW_PLAYING) {
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBar(
                        containerColor = Color.Transparent,
                        modifier = Modifier.glassBar()
                    ) {
                        val currentDestination = backStackEntry?.destination
                        TopLevelDestination.entries.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        // Evita apilar copias y conserva el estado de cada pestaña.
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Neon.Cyan,
                                    selectedTextColor = Neon.Cyan,
                                    indicatorColor = Neon.Cyan.copy(alpha = 0.18f),
                                    unselectedIconColor = Neon.OnDarkMuted,
                                    unselectedTextColor = Neon.OnDarkMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LIBRARY,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Routes.LIBRARY) {
                LibraryScreen()
            }

            composable(Routes.SONGS) {
                SongsScreen()
            }

            composable(Routes.FOLDERS) {
                FoldersScreen()
            }

            composable(Routes.SETTINGS) {
                SettingsScreen()
            }

            composable(Routes.PLAYLISTS) {
                PlaylistManagementScreen(
                    onOpenNowPlaying = { navController.navigate(Routes.NOW_PLAYING) }
                )
            }

            composable(Routes.NOW_PLAYING) {
                NowPlayingScreen(
                    onEditSong = { songId ->
                        navController.navigate(Routes.songEditor(songId))
                    }
                )
            }

            composable(
                route = Routes.SONG_EDITOR,
                arguments = listOf(
                    navArgument(Routes.SONG_EDITOR_ARG_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val songId = entry.arguments?.getLong(Routes.SONG_EDITOR_ARG_ID) ?: return@composable
                SongEditorScreen(
                    songId = songId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
