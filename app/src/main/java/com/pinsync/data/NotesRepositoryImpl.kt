package com.pinsync.data

import com.pinsync.api.PinApiService
import kotlinx.coroutines.flow.flow
import java.util.UUID

class NotesRepositoryImpl (private val apiService: PinApiService) : NotesRepository {
    override fun getAllNotes() = flow { emit(apiService.getNotes()) }
    override suspend fun favoriteNote(uuid: UUID) = apiService.favorite(uuid)
    override suspend fun unfavoriteNote(uuid: UUID) = apiService.unfavorite(uuid)

}