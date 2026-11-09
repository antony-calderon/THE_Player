package com.antuan.the_player.data.repository

import com.antuan.the_player.data.local.database.dao.SongMetadataDao
import com.antuan.the_player.data.local.database.entity.SongMetadataEntity
import com.antuan.the_player.domain.model.SongMetadata
import com.antuan.the_player.domain.repository.SongMetadataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongMetadataRepositoryImpl @Inject constructor(
    private val dao: SongMetadataDao
) : SongMetadataRepository {

    override fun observeMetadata(songId: Long): Flow<SongMetadata?> =
        dao.observe(songId).map { it?.toDomain() }

    override suspend fun getMetadata(songId: Long): SongMetadata? =
        dao.get(songId)?.toDomain()

    override suspend fun setArtwork(songId: Long, artworkUri: String?) {
        // Preservamos la letra existente al tocar solo la portada.
        val current = dao.get(songId)
        dao.upsert(
            (current ?: SongMetadataEntity(songId = songId)).copy(
                customArtworkUri = artworkUri,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun setLyrics(songId: Long, lyrics: String?) {
        val current = dao.get(songId)
        dao.upsert(
            (current ?: SongMetadataEntity(songId = songId)).copy(
                lyrics = lyrics,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun SongMetadataEntity.toDomain() = SongMetadata(
        songId = songId,
        customArtworkUri = customArtworkUri,
        lyrics = lyrics
    )
}
