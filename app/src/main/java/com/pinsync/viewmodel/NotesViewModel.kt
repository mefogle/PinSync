package com.pinsync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pinsync.api.PinApi
import com.pinsync.data.NotesPagingSource
import com.pinsync.data.NotesRepository
import com.pinsync.data.ObjectWithNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class NotesViewModel (private val notesRepository: NotesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUIState(loading = true))
    private val _newUiState = MutableStateFlow<NewNotesUIState<PagingData<PinApi.Object>>> (NewNotesUIState.Loading)
    private var _allNotes : LiveData<List<ObjectWithNote>>
    var allNotes : LiveData<List<ObjectWithNote>>
    val uiState: StateFlow<NotesUIState> = _uiState
    private var currentPagingSource: NotesPagingSource? = null
    val newUiState: StateFlow<NewNotesUIState<PagingData<PinApi.Object>>> = _newUiState.asStateFlow()
    //val pagingDataFlow: Flow<PagingData<PinApi.Object>> = notesRepository.getNotes().cachedIn(viewModelScope)
    val pager = Pager(
        config = PagingConfig(pageSize = 5, initialLoadSize = 10),
        pagingSourceFactory = { NotesPagingSource(PinApi.pinApiService).also {currentPagingSource = it} })
    val pagingDataFlow : Flow<PagingData<PinApi.Object>> = pager.flow.cachedIn(viewModelScope)

    private var timer: Timer? = null

    init {
        // Initialize the UI state to loading
        _uiState.value = NotesUIState(loading = true)
        _allNotes = notesRepository.getObjectsWithNotes()
        allNotes = _allNotes
        //fetchNotes()
        viewModelScope.launch {
            pagingDataFlow.collectLatest { pagingData ->
                _newUiState.value = NewNotesUIState.Success(pagingData)
            }
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
                viewModelScope.launch (Dispatchers.IO) {notesRepository.refreshNotes()}
                currentPagingSource?.invalidate()
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

sealed class NewNotesUIState<out T> {
    val selectedNotes: Set<UUID> = emptySet()
    data object Loading : NewNotesUIState<Nothing>()
    data class Success<T>(val data: T) :NewNotesUIState<T>()
    data class Error(val error: Throwable) :NewNotesUIState<Nothing>()
}