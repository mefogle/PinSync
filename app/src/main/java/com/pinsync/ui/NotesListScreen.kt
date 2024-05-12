package com.pinsync.ui

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pinsync.ui.components.NoteListItem
import com.pinsync.ui.navigation.PinSyncRoute
import com.pinsync.viewmodel.NotesUIState
import com.pinsync.viewmodel.NotesViewModel
import com.pinsync.viewmodel.ObjectsWithNotesLiveData

@Composable
fun NotesListScreen (
    navController: NavController,
    viewModel: NotesViewModel,
//    closeDetailScreen: () -> Unit,
//    navigateToDetail: (UUID) -> Unit,
//    addNew: () -> Unit,
    modifier: Modifier = Modifier
)
{
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    val newItems = (uiState as NotesUIState.Success<ObjectsWithNotesLiveData>).data.observeAsState().value
    newItems?.let { content ->
        Log.d("NotesListScreen", "size = ${content.size}")
        LazyColumn (modifier) {
            content.let { it ->
                items(newItems.size) { noteIndex ->
                    it[noteIndex].let { note ->
                        NoteListItem(
                            note.note,
                            navigateToDetail = {
                                 viewModel.selectNote(note)
                                 navController.navigate(PinSyncRoute.NOTE_DETAIL)
                            },
                            toggleFavorite = {
                                viewModel.setFavorite(
                                    note.container.uuid,
                                    !note.container.favorite
                                )
                            },
                            toggleSelection = {
                                viewModel.toggleSelectedNote(note.note.uuid)
                            },
                            isSelected = viewModel.listUiState.value.selectedNotes.contains(
                                note.note.uuid
                            ),
                            isFavorite = note.container.favorite
                        )
                    }
                }
            }
        }
    }
}