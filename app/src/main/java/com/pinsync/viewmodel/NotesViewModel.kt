package com.pinsync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinsync.data.NotesRepository
import com.pinsync.data.ObjectWithNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class NotesViewModel (private val notesRepository: NotesRepository) : ViewModel() {

    private val _newUiState = MutableStateFlow<NewNotesUIState<LiveData<List<ObjectWithNote>>>> (NewNotesUIState.Loading)
    private var _allNotes : LiveData<List<ObjectWithNote>>
    var allNotes : LiveData<List<ObjectWithNote>>
    val newUiState: StateFlow<NewNotesUIState<LiveData<List<ObjectWithNote>>>> = _newUiState.asStateFlow()

    private var timer: Timer? = null

    init {
        // Initialize the UI state to loading
        _newUiState.value = NewNotesUIState.Loading
        _allNotes = notesRepository.getObjectsWithNotes()
        allNotes = _allNotes
        //fetchNotes()
        viewModelScope.launch {
            _newUiState.value = NewNotesUIState.Success(allNotes)
        }
        startPeriodicTask()
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
            viewModelScope.launch (Dispatchers.IO) {notesRepository.refreshNote(uuid)}
        }
    }

    fun toggleSelectedNote(noteId: UUID) {
        val currentSelection = newUiState.value.selectedNotes
        _newUiState.value.selectedNotes = if (currentSelection.contains(noteId))
            currentSelection.minus(noteId) else currentSelection.plus(noteId)
    }

    private fun startPeriodicTask() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                viewModelScope.launch (Dispatchers.IO) {notesRepository.refreshNotes()}
                //currentPagingSource?.invalidate()
            }
        }, 0, 5000) // Schedule the task to run every 5 seconds
    }
    override fun onCleared() {
        super.onCleared()
        timer?.cancel() // Cancel the timer when the ViewModel is cleared
    }
}

sealed class NewNotesUIState<out T> {
    var selectedNotes: Set<UUID> = emptySet()
    data object Loading: NewNotesUIState<Nothing>()

    data class Success<T>(val data: T): NewNotesUIState<T>()
    data class Error(val error: Throwable): NewNotesUIState<Nothing>()
}