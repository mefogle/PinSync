package com.pinsync.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pinsync.PinSyncApp
import com.pinsync.api.PinApi
import com.pinsync.api.PinApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class NotesRepositoryImpl (private val apiService: PinApiService) : NotesRepository {
    private val objectDao = PinSyncApp.db?.objectDao()
    override fun getAllNotes() = flow { emit(apiService.getNotes()) }

    override fun getNotes(): Flow<PagingData<PinApi.Object>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { NotesPagingSource(apiService) }
        ).flow
    }

    override fun getNote(uuid: UUID): Flow<PinApi.Object> = flow { emit(apiService.getMemory(uuid)) }
    override fun getObjectsWithNotes(): LiveData<List<ObjectWithNote>> {
        return objectDao!!.getObjectsWithNotes()
    }

    override suspend fun refreshNote(uuid: UUID) {
        val container = apiService.getMemory(uuid)
        val objectEntity = mapObjectDtoToEntity(container)
        objectDao?.insertWithNote(objectEntity)
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
            val objectEntities: List<Object> = allNotes.map { dto -> mapObjectDtoToEntity(dto) }
            val validUUIDs = objectEntities.map { it.uuid }

            if (objectEntities.isNotEmpty()) {
                objectDao?.insertAllWithNotes(objectEntities)
                objectDao?.removeDeletedObjects(validUUIDs)
            } else {
                // The database is empty... or we think it is.
                objectDao?.removeAll()
            }
        }
        catch (e: Exception) {
            Log.e("NotesRepositoryImpl", "Unexpected error refreshNotes: ", e)
        }
    }

    override suspend fun favoriteNote(uuid: UUID) = apiService.favorite(uuid)
    override suspend fun unfavoriteNote(uuid: UUID) = apiService.unfavorite(uuid)

}