package com.pindroid.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pindroid.ui.components.NoteListItem
import com.pindroid.ui.components.PinDroidFAB
import com.pindroid.ui.navigation.PinDroidRoute.NOTE_DETAIL
import com.pindroid.viewmodel.NotesViewModel

@Composable
fun NotesListScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    val newItems = uiState.notes
    Scaffold(
        floatingActionButton = {
            PinDroidFAB(onClick = {
                viewModel.addNew()
                navController.navigate(NOTE_DETAIL)
            })
        })
    { innerPadding ->
        newItems.let { content ->
            LazyColumn(modifier.padding(innerPadding)) {
                content.let {
                    items(newItems.size) { noteIndex ->
                        it[noteIndex].let { note ->
                            NoteListItem(
                                note.note,
                                navigateToDetail = {
                                    viewModel.editExisting(note.container.uuid)
                                    navController.navigate(NOTE_DETAIL)
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
}