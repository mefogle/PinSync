package com.pinsync.data

import com.pinsync.api.PinApi
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * An Interface contract to retrieve, create and edit Notes
 */

interface NotesRepository {
    fun getAllNotes(): Flow<PinApi.Content>

    fun getNote(uuid: UUID): Flow<ObjectWithNote>

    fun getObjectsWithNotes () : Flow<List<ObjectWithNote>>

    suspend fun refreshNotes()

    suspend fun refreshNote(uuid: UUID)

    suspend fun deleteNote(uuid: UUID)

    suspend fun favoriteNote(uuid: UUID)

    suspend fun unfavoriteNote(uuid: UUID)
    suspend fun updateNote(uuid: UUID, note : Note): Flow<ObjectWithNote>
    suspend fun createNote(note: Note): Flow<ObjectWithNote>
}
