package com.antuan.the_player.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antuan.the_player.domain.model.ThemeMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickBackground = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Permiso persistente para seguir leyendo el fondo tras reiniciar.
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.setBackgroundUri(uri.toString())
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { TopAppBar(title = { Text("Ajustes") }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            SectionTitle("Tema")
            Column(Modifier.selectableGroup()) {
                ThemeOption("Según el sistema", settings.themeMode == ThemeMode.SYSTEM) {
                    viewModel.setThemeMode(ThemeMode.SYSTEM)
                }
                ThemeOption("Claro", settings.themeMode == ThemeMode.LIGHT) {
                    viewModel.setThemeMode(ThemeMode.LIGHT)
                }
                ThemeOption("Oscuro", settings.themeMode == ThemeMode.DARK) {
                    viewModel.setThemeMode(ThemeMode.DARK)
                }
            }

            HorizontalDivider()

            SectionTitle("Color")
            SwitchRow(
                title = "Color dinámico (Material You)",
                subtitle = "Usa los colores del fondo de pantalla en Android 12+",
                checked = settings.dynamicColor,
                onChange = viewModel::setDynamicColor
            )

            HorizontalDivider()

            SectionTitle("Reproducción")
            SwitchRow(
                title = "Quitar duplicados al fusionar",
                subtitle = "Evita repetir una canción presente en varias playlists",
                checked = settings.removeDuplicatesOnMerge,
                onChange = viewModel::setRemoveDuplicates
            )

            HorizontalDivider()

            SectionTitle("Fondo personalizado")
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (settings.backgroundUri != null) "Imagen de fondo activa"
                    else "Sin imagen de fondo (se usa el color del tema)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { pickBackground.launch(arrayOf("image/*")) }) {
                        Text("Elegir imagen")
                    }
                    if (settings.backgroundUri != null) {
                        OutlinedButton(
                            onClick = { viewModel.setBackgroundUri(null) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Quitar")
                        }
                    }
                }

                // Slider de opacidad; solo tiene sentido con fondo activo.
                Text(
                    text = "Opacidad del fondo: ${(settings.backgroundOpacity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Slider(
                    value = settings.backgroundOpacity,
                    onValueChange = viewModel::setBackgroundOpacity,
                    valueRange = 0f..1f,
                    enabled = settings.backgroundUri != null
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
