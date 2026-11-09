package com.antuan.the_player.data.local.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.antuan.the_player.domain.model.MusicFolder
import com.antuan.the_player.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente de datos que lee las canciones locales del [MediaStore] de Android.
 *
 * El MediaStore es la fuente de verdad de las canciones; esta app no las
 * copia a su base de datos, solo las referencia por su id. Todas las consultas
 * se ejecutan en [Dispatchers.IO] porque acceden a disco.
 */
@Singleton
class MediaStoreDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.MIME_TYPE
    )

    /**
     * Devuelve todas las canciones de música del dispositivo, ordenadas por
     * título. Filtra por IS_MUSIC para excluir tonos, notificaciones, etc.
     */
    suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        query(selection, selectionArgs = null, sortOrder)
    }

    /**
     * Agrupa las canciones del dispositivo por su carpeta física de almacenamiento,
     * derivada de la ruta del archivo (columna DATA).
     *
     * @return Lista de carpetas ordenadas por nombre, cada una con sus canciones.
     */
    suspend fun getFolders(): List<MusicFolder> = withContext(Dispatchers.IO) {
        getAllSongs()
            .groupBy { song -> song.path.substringBeforeLast('/', missingDelimiterValue = "") }
            .filterKeys { it.isNotEmpty() }
            .map { (path, songs) ->
                MusicFolder(
                    path = path,
                    name = path.substringAfterLast('/').ifEmpty { path },
                    songs = songs
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    /**
     * Devuelve las canciones cuyos ids se pasan. Útil para resolver las
     * referencias guardadas en Room. El orden de MediaStore no se garantiza,
     * así que el llamante debe reordenar según sus necesidades.
     */
    suspend fun getSongsByIds(ids: List<Long>): List<Song> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext emptyList()

        val placeholders = ids.joinToString(separator = ",") { "?" }
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media._ID} IN ($placeholders)"
        val selectionArgs = ids.map { it.toString() }.toTypedArray()

        query(selection, selectionArgs, sortOrder = null)
    }

    private fun query(
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): List<Song> {
        val songs = ArrayList<Song>()

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                songs.add(cursor.toSong(idCol, titleCol, artistCol, albumCol, albumIdCol, durationCol, trackCol, yearCol, dataCol, sizeCol, mimeCol))
            }
        }
        return songs
    }

    private fun Cursor.toSong(
        idCol: Int, titleCol: Int, artistCol: Int, albumCol: Int, albumIdCol: Int,
        durationCol: Int, trackCol: Int, yearCol: Int, dataCol: Int, sizeCol: Int, mimeCol: Int
    ): Song {
        val id = getLong(idCol)
        val uri = ContentUris.withAppendedId(collection, id)
        val track = if (isNull(trackCol)) null else getInt(trackCol).takeIf { it > 0 }
        val year = if (isNull(yearCol)) null else getInt(yearCol).takeIf { it > 0 }

        return Song(
            id = id,
            uri = uri,
            title = getString(titleCol) ?: "Desconocido",
            artist = getString(artistCol) ?: "Artista desconocido",
            album = getString(albumCol) ?: "Álbum desconocido",
            albumId = getLong(albumIdCol),
            durationMs = getLong(durationCol),
            trackNumber = track,
            year = year,
            path = getString(dataCol).orEmpty(),
            sizeBytes = getLong(sizeCol),
            mimeType = getString(mimeCol)
        )
    }
}
