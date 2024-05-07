package com.pinsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinsync.api.PinApi
import com.pinsync.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class NotesViewModel (private val notesRepository: NotesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUIState(loading = true))
    val uiState: StateFlow<NotesUIState> = _uiState
    private var timer: Timer? = null

    init {
        // Initialize the UI state to loading
        _uiState.value = NotesUIState(loading = true)
        fetchNotes()
        startPeriodicTask()
    }

    private fun fetchNotes() {
        viewModelScope.launch {
            notesRepository.getAllNotes()
                .catch { e ->
                    _uiState.value = NotesUIState(error = e.message)
                }
                .collect { content ->
                    val noteMap =  mutableMapOf<UUID, PinApi.Object>()
                    for (note in content.content) {
                        noteMap[note.uuid] = note
                    }
                    _uiState.value = NotesUIState(notes = noteMap, loading = false)
                }
        }
    }

    private fun fetchNote(uuid: UUID) {
        viewModelScope.launch {
            notesRepository.getNote(uuid)
                .catch { e ->
                    _uiState.value = NotesUIState(error = e.message)
                }
                .collect { content ->
                    val notesMap = uiState.value.notes.toMutableMap()
                    notesMap[uuid] = content
                    _uiState.value = NotesUIState(notes = notesMap, loading = false)
                }
        }
    }

    // Note that the UUID here is the UUID of the content envelope, not the UUID of the NoteData.
    fun setFavorite (uuid: UUID, isFavorite: Boolean){
        viewModelScope.launch {
            // Note that we need to pass in the UUID of the content element,
            // not the UUID of the NoteData.
            if (isFavorite) {
                notesRepository.favoriteNote(uuid)
            } else {
                notesRepository.unfavoriteNote(uuid)
            }
            // This will make the favorite state update more quickly.
            fetchNote(uuid)
        }
    }

    fun toggleSelectedNote(noteId: UUID) {
        val currentSelection = uiState.value.selectedNotes
        _uiState.value = _uiState.value.copy(
            selectedNotes = if (currentSelection.contains(noteId))
                currentSelection.minus(noteId) else currentSelection.plus(noteId)
        )
    }

    private fun startPeriodicTask() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                fetchNotes()
            }
        }, 0, 5000) // Schedule the task to run every 5 seconds
    }
    override fun onCleared() {
        super.onCleared()
        timer?.cancel() // Cancel the timer when the ViewModel is cleared
    }
}

data class NotesUIState(
    val notes: Map<UUID, PinApi.Object> = emptyMap(),
    // The UUIDs in this set are the UUIDs of the content elements that are selected.
    val selectedNotes: Set<UUID> = emptySet(),
    val loading: Boolean = false,
    val error: String? = null
)