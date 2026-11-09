package com.antuan.the_player

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application de la app. [HiltAndroidApp] genera el contenedor de
 * dependencias de Hilt y es requisito para usar @AndroidEntryPoint en
 * Activities, Services (como PlaybackService), etc.
 */
@HiltAndroidApp
class ReproductorApp : Application()
