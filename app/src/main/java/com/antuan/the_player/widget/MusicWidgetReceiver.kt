package com.antuan.the_player.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver que el sistema usa para alojar el [MusicWidget] en la pantalla de
 * inicio. Se declara en el AndroidManifest.
 */
class MusicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MusicWidget()
}
