package com.pinsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinsync.data.ContentObject
import com.pinsync.data.ContentType
import com.pinsync.data.Note
import com.pinsync.data.NoteData
import com.pinsync.data.NotesRepository
import com.pinsync.data.ObjectWithNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class NotesViewModel (private val notesRepository: NotesRepository) : ViewModel() {

    private val _listUiState = MutableStateFlow(NoteListUIState (loading = true))
    val listUiState: StateFlow<NoteListUIState> = _listUiState
    private val _detailUiState = MutableStateFlow(NoteDetailUIState (loading = true))
    val detailUiState: StateFlow<NoteDetailUIState> = _detailUiState

    private var timer: Timer? = null

    fun editExisting(noteId: UUID) {
        _detailUiState.value = NoteDetailUIState (loading = true)
        viewModelScope.launch (Dispatchers.IO) {
            val note = notesRepository.getNote(noteId)
                .catch {ex ->
                    _detailUiState.value = NoteDetailUIState (error = ex.message)
                }
                .first()
            _detailUiState.value = _detailUiState.value.copy (loading = false, isDetailOnlyOpen = true, currentNote = note)
        }
    }

    fun addNew() {
        // This allows us to treat a new note as a special case of an updated note.  By not specifying
        // a UUID, an empty one will be used instead.
        val emptyNote = ObjectWithNote(
            ContentObject(contentType = ContentType.GENERIC_NOTE), note = NoteData(note = Note()))
        _detailUiState.value = _detailUiState.value.copy (loading = false, isDetailOnlyOpen = true, currentNote = emptyNote)
    }

    fun updateNote (note: ObjectWithNote) {
        _detailUiState.value = NoteDetailUIState (loading = true)
        viewModelScope.launch (Dispatchers.IO){
            // Look for the special UUID that indicates a new item
            if (note.container.uuid != UUID(0, 0)) {
                notesRepository.updateNote(note.container.uuid, note.note.note)
                    .catch {ex ->
                        _detailUiState.value = NoteDetailUIState (error = ex.message)
                    }
                    .first ()
                _detailUiState.value = _detailUiState.value.copy (loading = false, isDetailOnlyOpen = false)
            }
            else {
                notesRepository.createNote(note.note.note)
                    .catch {ex ->
                        _detailUiState.value = NoteDetailUIState (error = ex.message)
                    }
                    .first ()
                _detailUiState.value = _detailUiState.value.copy (loading = false, isDetailOnlyOpen = false)

            }
        }
    }

    init {
        // Initialize the UI state to loading
        //_listUiState.value = NotesUIState.Loading
        //_allNotes = notesRepository.getObjectsWithNotes()
        //allNotes = _allNotes
//        viewModelScope.launch {
//            _listUiState.value = NotesUIState.Success(emptySet(), false, allNotes)
//        }
        viewModelScope.launch (Dispatchers.IO) {
            notesRepository.getObjectsWithNotes()
                .catch {ex ->
                    _listUiState.value = NoteListUIState (error = ex.message)
                }
                .collect { notes->
                    _listUiState.value = NoteListUIState (notes = notes)
                }
        }
        startPeriodicTask()
    }

    fun deleteNote(note: ObjectWithNote) {
        viewModelScope.launch (Dispatchers.IO) {
            notesRepository.deleteNote(note.container.uuid)
            _detailUiState.value = _detailUiState.value.copy (isDetailOnlyOpen = false)
        }
    }

    // Note that the UUID here is the UUID of the content envelope, not the UUID of the NoteData.
    fun setFavorite (uuid: UUID, isFavorite: Boolean){
        viewModelScope.launch (Dispatchers.IO) {
            // Note that we need to pass in the UUID of the content element,
            // not the UUID of the NoteData.
            if (isFavorite) {
                notesRepository.favoriteNote(uuid)
            } else {
                notesRepository.unfavoriteNote(uuid)
            }
        }
    }

    fun toggleSelectedNote(noteId: UUID) {
        val currentSelection = listUiState.value.selectedNotes
        _listUiState.value = _listUiState.value.copy(
            selectedNotes = if (currentSelection.contains(noteId))
                currentSelection.minus(noteId) else currentSelection.plus(noteId)
        )
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

data class NoteListUIState (
    val notes: List<ObjectWithNote> = emptyList(),
    val selectedNotes: Set<UUID> = emptySet(),
    val loading : Boolean = false,
    val error : String? = null
)
data class NoteDetailUIState (
    val isDetailOnlyOpen: Boolean = false,
    val currentNote: ObjectWithNote? = null,
    val loading : Boolean = false,
    val error : String? = null
)