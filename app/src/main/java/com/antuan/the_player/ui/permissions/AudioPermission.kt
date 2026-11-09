package com.antuan.the_player.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Nombre del permiso de lectura de audio según la versión de Android.
 *
 * - Android 13 (API 33, TIRAMISU) y superior: READ_MEDIA_AUDIO (permiso granular).
 * - Android 12L e inferior: READ_EXTERNAL_STORAGE.
 */
val audioReadPermission: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

/** Comprueba si el permiso de lectura de audio ya está concedido. */
fun Context.hasAudioPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, audioReadPermission) ==
        PackageManager.PERMISSION_GRANTED
