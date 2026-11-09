package com.antuan.the_player.widget

import com.antuan.the_player.player.PlaybackStateHolder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Puente de Hilt para que el widget (que no es un componente inyectable como
 * una Activity) acceda a dependencias singleton de la app, en concreto al
 * estado de reproducción compartido.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun playbackStateHolder(): PlaybackStateHolder
}
