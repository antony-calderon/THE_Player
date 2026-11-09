package com.antuan.the_player.ui.components

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage

/** URI base del arte de álbum del MediaStore. */
private val ALBUM_ART_BASE: Uri = Uri.parse("content://media/external/audio/albumart")

/** Construye la URI de la carátula de un álbum a partir de su id. */
fun albumArtUri(albumId: Long): Uri = ContentUris.withAppendedId(ALBUM_ART_BASE, albumId)

/**
 * Carátula de álbum con Coil. Muestra la portada del álbum del MediaStore y,
 * si no existe o falla, un icono de nota musical como fallback.
 *
 * @param model Fuente de imagen: puede ser una URI de portada custom (String)
 *   o el albumId resuelto con [albumArtUri]. Null muestra el placeholder.
 */
@Composable
fun AlbumArt(
    model: Any?,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 8
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius.dp())),
        contentAlignment = Alignment.Center
    ) {
        if (model != null) {
            SubcomposeAsyncImage(
                model = model,
                contentDescription = "Carátula",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = { Placeholder() },
                loading = { Placeholder() }
            )
        } else {
            Placeholder()
        }
    }
}

@Composable
private fun Placeholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp())),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Pequeño helper para no importar androidx.compose.ui.unit.dp en dos sitios.
private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())
