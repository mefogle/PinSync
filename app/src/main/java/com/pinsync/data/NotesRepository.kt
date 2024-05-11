package com.pinsync.data

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.pinsync.api.PinApi
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.util.UUID

/**
 * An Interface contract to retrieve, create and edit Notes
 */

interface NotesRepository {
    fun getAllNotes(): Flow<PinApi.Content>

    fun getNotes() : Flow<PagingData<PinApi.Object>>

    fun getNote(uuid: UUID): Flow<PinApi.Object>

    fun getObjectsWithNotes () : LiveData<List<ObjectWithNote>>

    suspend fun refreshNotes()

    suspend fun favoriteNote(uuid: UUID) : Response<Unit>

    suspend fun unfavoriteNote(uuid: UUID) : Response<Unit>
}
