package com.antuan.the_player.ui.nowplaying

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extrae el color dominante de una imagen (carátula) usando Coil para cargarla
 * y Palette para analizarla. Devuelve [fallback] mientras carga o si falla.
 *
 * @param model Fuente de imagen (URI de portada custom o albumArtUri).
 */
@Composable
fun rememberDominantColor(model: Any?, fallback: Color): State<Color> {
    val context = LocalContext.current
    val loader = remember { ImageLoader(context) }

    return produceState(initialValue = fallback, key1 = model) {
        if (model == null) {
            value = fallback
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(model)
                    .allowHardware(false) // Palette necesita leer los píxeles.
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.drawable.toBitmap()
                    val palette = Palette.from(bitmap).generate()
                    val swatch = palette.vibrantSwatch
                        ?: palette.mutedSwatch
                        ?: palette.dominantSwatch
                    swatch?.rgb?.let { Color(it) } ?: fallback
                } else {
                    fallback
                }
            } catch (e: Exception) {
                fallback
            }
        }
    }
}
