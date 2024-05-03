package com.pinsync.data

import com.pinsync.api.PinApi
import kotlinx.coroutines.flow.Flow

/**
 * An Interface contract to retrieve, create and edit Notes
 */

interface NotesRepository {
    fun getAllNotes(): Flow<PinApi.Content>
}
