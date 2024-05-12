package com.pinsync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

typealias ObjectsWithNotesLiveData = LiveData<List<ObjectWithNote>>
typealias ObjectWithNoteLiveData = LiveData<ObjectWithNote>

class NotesViewModel (private val notesRepository: NotesRepository) : ViewModel() {

    private val _listUiState = MutableStateFlow<NotesUIState<ObjectsWithNotesLiveData>> (NotesUIState.Loading)
    val listUiState: StateFlow<NotesUIState<ObjectsWithNotesLiveData>> = _listUiState.asStateFlow()
    private var _allNotes : ObjectsWithNotesLiveData
    var allNotes : ObjectsWithNotesLiveData

    private val _detailUiState = MutableStateFlow<NoteUIState<ObjectWithNoteLiveData>> (NoteUIState.Loading)
    val detailUiState: StateFlow<NoteUIState<ObjectWithNoteLiveData>> = _detailUiState.asStateFlow()
    private var _selectedNote : ObjectWithNoteLiveData = MutableLiveData()
//    var selectedNote : ObjectWithNoteLiveData = _selectedNote

    private var timer: Timer? = null



    fun selectNote(note: ObjectWithNote) {
        _selectedNote = notesRepository.getNote(note.container.uuid)
        viewModelScope.launch {
            _detailUiState.value = NoteUIState.Success(_selectedNote)
        }
    }

    init {
        // Initialize the UI state to loading
        _listUiState.value = NotesUIState.Loading
        _allNotes = notesRepository.getObjectsWithNotes()
        allNotes = _allNotes
        viewModelScope.launch {
            _listUiState.value = NotesUIState.Success(emptySet(), false, allNotes)
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
        val currentSelection = listUiState.value.selectedNotes
        val newSelection = if (currentSelection.contains(noteId))
            currentSelection.minus(noteId) else currentSelection.plus(noteId)
        _listUiState.value = NotesUIState.Success(newSelection, false, allNotes)
    }

    private fun startPeriodicTask() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                viewModelScope.launch (Dispatchers.IO) {notesRepository.refreshNotes()}
            }
        }, 0, 5000) // Schedule the task to run every 5 seconds
    }
    override fun onCleared() {
        super.onCleared()
        timer?.cancel() // Cancel the timer when the ViewModel is cleared
    }
}

sealed class NotesUIState<out T> {
    open val selectedNotes: Set<UUID> = emptySet()
    open val isDetailOnlyOpen: Boolean = false
    data object Loading: NotesUIState<Nothing>()
    data class Success<T>(override val selectedNotes : Set<UUID>, override val isDetailOnlyOpen: Boolean, val data: T): NotesUIState<T>()
    data class Error(val error: Throwable): NotesUIState<Nothing>()
}

sealed class NoteUIState<out T> {
    data object Loading: NoteUIState<Nothing>()
    data class Success<T>(val data: T): NoteUIState<T>()
//    data class Error(val error: Throwable): NoteUIState<Nothing>()
}