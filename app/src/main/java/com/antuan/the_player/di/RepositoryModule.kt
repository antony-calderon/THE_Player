package com.antuan.the_player.di

import com.antuan.the_player.data.repository.PlaylistRepositoryImpl
import com.antuan.the_player.data.repository.SettingsRepositoryImpl
import com.antuan.the_player.data.repository.SongMetadataRepositoryImpl
import com.antuan.the_player.domain.repository.PlaylistRepository
import com.antuan.the_player.domain.repository.SettingsRepository
import com.antuan.the_player.domain.repository.SongMetadataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enlaza las interfaces de repositorio (capa de dominio) con sus
 * implementaciones concretas (capa de datos).
 *
 * Usamos @Binds (en un módulo abstracto) en lugar de @Provides porque solo
 * estamos mapeando una interfaz a su implementación: es más eficiente y es la
 * forma idiomática en Hilt. MediaStoreDataSource y PlaylistRepositoryImpl se
 * construyen por constructor (@Inject), así que Hilt sabe crearlos sin más.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindSongMetadataRepository(
        impl: SongMetadataRepositoryImpl
    ): SongMetadataRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
