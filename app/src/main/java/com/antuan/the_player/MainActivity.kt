package com.antuan.the_player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.antuan.the_player.ui.navigation.AppNavGraph
import com.antuan.the_player.ui.permissions.AudioPermissionGate
import com.antuan.the_player.ui.permissions.AudioPermissionStatus
import com.antuan.the_player.ui.settings.SettingsViewModel
import com.antuan.the_player.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Leemos las preferencias para aplicar el tema elegido por el usuario.
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            AppTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                val bg = settings.backgroundUri

                Box(Modifier.fillMaxSize()) {
                    if (bg != null) {
                        // 1) Imagen de fondo a pantalla completa, opaca.
                        AsyncImage(
                            model = bg,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // 2) Velo translúcido encima: a mayor backgroundOpacity,
                        //    más se atenúa la imagen hacia el color de superficie.
                        //    Así el contenido de la UI sigue siendo legible.
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surface
                                        .copy(alpha = settings.backgroundOpacity)
                                )
                        )
                    }

                    // Surface transparente si hay fondo (para que la imagen se
                    // vea en toda la app); color del tema si no lo hay.
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (bg != null) Color.Transparent
                        else MaterialTheme.colorScheme.background
                    ) {
                        AudioPermissionGate(
                            grantedContent = { AppNavGraph() },
                            deniedContent = { status, onRequest, onOpenSettings ->
                                PermissionDeniedContent(status, onRequest, onOpenSettings)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    status: AudioPermissionStatus,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permiso de audio necesario",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Para mostrar tu música local, la app necesita acceso a los archivos de audio del dispositivo.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        if (status == AudioPermissionStatus.PERMANENTLY_DENIED) {
            Text(
                text = "Lo denegaste permanentemente. Concédelo desde los ajustes de la app.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(onClick = onOpenSettings) { Text("Abrir ajustes") }
        } else {
            Button(onClick = onRequest) { Text("Conceder permiso") }
        }
    }
}
