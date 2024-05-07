package com.pinsync.data

import com.pinsync.api.PinApi
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.util.UUID

/**
 * An Interface contract to retrieve, create and edit Notes
 */

interface NotesRepository {
    fun getAllNotes(): Flow<PinApi.Content>

    fun getNote(uuid: UUID): Flow<PinApi.Object>

    suspend fun favoriteNote(uuid: UUID) : Response<Unit>

    suspend fun unfavoriteNote(uuid: UUID) : Response<Unit>
}
