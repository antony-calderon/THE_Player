package com.antuan.the_player.ui.permissions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

/**
 * Estado del permiso de audio, pensado para dirigir la UI de forma reactiva.
 */
enum class AudioPermissionStatus {
    /** Aún no sabemos / no se ha solicitado. */
    UNKNOWN,

    /** Concedido: ya se puede leer la biblioteca. */
    GRANTED,

    /** Denegado, pero aún se puede volver a pedir. */
    DENIED,

    /**
     * Denegado de forma permanente ("no volver a preguntar"). La única vía es
     * que el usuario lo conceda manualmente desde los ajustes de la app.
     */
    PERMANENTLY_DENIED
}

/**
 * Gate de permiso de audio: muestra [grantedContent] cuando el permiso está
 * concedido y [deniedContent] en cualquier otro caso, entregándole a este
 * último el estado actual y dos acciones (reintentar / abrir ajustes).
 *
 * Usa la API estándar de AndroidX (rememberLauncherForActivityResult), sin
 * dependencias externas, para no arrastrar versiones transitivas de Compose.
 *
 * @param grantedContent Contenido a mostrar cuando el permiso está concedido
 *   (normalmente, la biblioteca de música).
 * @param deniedContent UI de solicitud/explicación. Recibe el estado y las
 *   acciones para pedir el permiso o abrir los ajustes del sistema.
 */
@Composable
fun AudioPermissionGate(
    grantedContent: @Composable () -> Unit,
    deniedContent: @Composable (
        status: AudioPermissionStatus,
        onRequest: () -> Unit,
        onOpenSettings: () -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var status by remember {
        mutableStateOf(
            if (context.hasAudioPermission()) AudioPermissionStatus.GRANTED
            else AudioPermissionStatus.UNKNOWN
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        status = when {
            granted -> AudioPermissionStatus.GRANTED
            // Si tras denegar el sistema ya no mostraría el diálogo, es permanente.
            activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, audioReadPermission) ->
                AudioPermissionStatus.PERMANENTLY_DENIED
            else -> AudioPermissionStatus.DENIED
        }
    }

    // Primera solicitud automática la primera vez que entramos sin permiso.
    LaunchedEffect(Unit) {
        if (status == AudioPermissionStatus.UNKNOWN) {
            launcher.launch(audioReadPermission)
        }
    }

    when (status) {
        AudioPermissionStatus.GRANTED -> grantedContent()
        else -> deniedContent(
            status,
            { launcher.launch(audioReadPermission) },
            {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
        )
    }
}
