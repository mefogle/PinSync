package com.pinsync.data

import android.util.Log
import com.pinsync.PinSyncApplication
import com.pinsync.api.PinApi
import com.pinsync.api.PinApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class NotesRepositoryImpl (private val apiService: PinApiService) : NotesRepository {
    private val objectDao = PinSyncApplication.db.objectDao()
    override fun getAllNotes() = flow { emit(apiService.getNotes()) }
    override fun getNote(uuid: UUID): Flow<ObjectWithNote> {
        return objectDao.getObjectWithNote(uuid)
    }
    override fun getObjectsWithNotes(): Flow<List<ObjectWithNote>> {
        return objectDao.getObjectsWithNotes()
    }

    override suspend fun refreshNote(uuid: UUID) {
        val container = apiService.getMemory(uuid)
        val objectEntity = mapObjectDtoToEntity(container)
        objectDao.insertWithNote(objectEntity)
    }

    override suspend fun deleteNote(uuid: UUID) {
        apiService.delete(uuid)
        refreshNotes()
    }
    override suspend fun refreshNotes() {
        val allNotes = mutableListOf<PinApi.Object>()
        var moreData = true
        var nextPage = 0
        while (moreData) {
            val container = apiService.getNotes(nextPage)
            val objectDtos = container.content
            allNotes.addAll(objectDtos)
            moreData = !container.last
            nextPage++
        }

        // For some reason, a null dto shows up every once in a while.  This is a workaround.

        try {
            val contentObjectEntities: List<ContentObject> = allNotes.map { dto -> mapObjectDtoToEntity(dto) }
            val validUUIDs = contentObjectEntities.map { it.uuid }

            if (contentObjectEntities.isNotEmpty()) {
                objectDao.insertAllWithNotes(contentObjectEntities)
                objectDao.removeDeletedObjects(validUUIDs)
            } else {
                // The database is empty... or we think it is.
                objectDao.removeAll()
            }
        }
        catch (e: Exception) {
            Log.e("NotesRepositoryImpl", "Unexpected error in refreshNotes: ", e)
        }
    }

    override suspend fun favoriteNote(uuid: UUID) {
        apiService.favorite(uuid)
        refreshNote(uuid)
    }
    override suspend fun unfavoriteNote(uuid: UUID) {
        apiService.unfavorite(uuid)
        refreshNote(uuid)
    }
    override suspend fun updateNote(uuid: UUID, note: Note): Flow<ObjectWithNote> {
        // When updating, it's important that the uuid not be specified in the note body.
        val container = apiService.updateNote(uuid,
            PinApi.NoteCreateDTO(note.title, note.text)
        )

        val objectEntity = mapObjectDtoToEntity(container)
        objectDao.insertWithNote(objectEntity)
        return objectDao.getObjectWithNote(objectEntity.uuid)
    }

    override suspend fun createNote(note: Note): Flow<ObjectWithNote> {
        // When creating, it's important that the uuid not be specified in the note body.
        val container = apiService.createNote(
            PinApi.NoteCreateDTO(note.title, note.text)
        )
        val objectEntity = mapObjectDtoToEntity(container)
        objectDao.insertWithNote(objectEntity)
        return objectDao.getObjectWithNote(objectEntity.uuid)
    }
}