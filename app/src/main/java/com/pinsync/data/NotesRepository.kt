package com.pinsync.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * An Interface contract to retrieve, create and edit Notes
 * The application should always be accessing data through the Repository, not through the
 * API.
 */

interface NotesRepository {

    fun getNote(uuid: UUID): Flow<ObjectWithNote>

    fun getObjectsWithNotes(): Flow<List<ObjectWithNote>>

    // This will do a full refresh and should be used sparingly.  It will check all elements of
    // the DB against the elements coming from the server and remove any that are no longer on the
    // server.
    suspend fun refreshNotes()

    // This will perform a "smart" refresh, first checking the time of the last modification along
    // with the last known count of records to see if any have been added or potentially deleted
    // and, if so, do a full refresh. What this can't catch (because timestamps aren't modified and
    // the records aren't sortable by modified time) is cases where the only changes are existing
    // records that have been updated. The method will return true if a full refresh was performed.
    suspend fun refreshIfNeeded() : Boolean

    suspend fun refreshNote(uuid: UUID)

    suspend fun deleteNote(uuid: UUID)

    suspend fun favoriteNote(uuid: UUID)

    suspend fun unfavoriteNote(uuid: UUID)
    suspend fun updateNote(uuid: UUID, note: Note): Flow<ObjectWithNote>
    suspend fun createNote(note: Note): Flow<ObjectWithNote>
}
