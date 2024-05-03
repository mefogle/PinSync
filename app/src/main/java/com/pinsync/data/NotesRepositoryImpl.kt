package com.pinsync.data

import com.pinsync.api.PinApiService
import kotlinx.coroutines.flow.flow

class NotesRepositoryImpl (private val apiService: PinApiService) : NotesRepository {
    override fun getAllNotes() = flow { emit(apiService.getNotes()) }
}