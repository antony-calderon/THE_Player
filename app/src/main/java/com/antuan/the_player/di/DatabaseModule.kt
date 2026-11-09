package com.antuan.the_player.di

import android.content.Context
import androidx.room.Room
import com.antuan.the_player.data.local.database.MusicDatabase
import com.antuan.the_player.data.local.database.dao.PlaylistDao
import com.antuan.the_player.data.local.database.dao.SongMetadataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provisión de la base de datos Room y sus DAOs.
 *
 * Se instala en [SingletonComponent] porque la base de datos debe ser única en
 * toda la app. El [Context] de aplicación lo inyecta Hilt automáticamente con
 * el calificador [ApplicationContext]; no hace falta proveerlo a mano.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase =
        Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            MusicDatabase.DATABASE_NAME
        )
            // En desarrollo recreamos la BD si cambia el esquema. En producción,
            // sustituir por migraciones explícitas con addMigrations(...).
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao =
        database.playlistDao()

    @Provides
    fun provideSongMetadataDao(database: MusicDatabase): SongMetadataDao =
        database.songMetadataDao()
}
