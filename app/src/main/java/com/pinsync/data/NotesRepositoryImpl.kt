package com.pinsync.data

import com.pinsync.api.PinApi
import com.pinsync.api.PinApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class NotesRepositoryImpl (private val apiService: PinApiService) : NotesRepository {
    override fun getAllNotes() = flow { emit(apiService.getNotes()) }
    override fun getNote(uuid: UUID): Flow<PinApi.Object> = flow { emit(apiService.getMemory(uuid)) }

    override suspend fun favoriteNote(uuid: UUID) = apiService.favorite(uuid)
    override suspend fun unfavoriteNote(uuid: UUID) = apiService.unfavorite(uuid)

}