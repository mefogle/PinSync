package com.pinsync

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pinsync.api.PinApi
import com.pinsync.data.NotesRepositoryImpl
import com.pinsync.data.ObjectWithNote
import com.pinsync.ui.AuthDialog
import com.pinsync.ui.components.NoteListItem
import com.pinsync.ui.theme.PinSyncTheme
import com.pinsync.viewmodel.NotesUIState
import com.pinsync.viewmodel.NotesViewModel
import com.pinsync.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: NotesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinSyncTheme {
                var showDialog by remember { mutableStateOf(true) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // We only need to display the authentication dialog if we aren't already authenticated
                    if (!PinApi.isAuthenticated() && showDialog) {
                        AuthDialog(onDismissRequest = { showDialog = false })
                    } else {
                        viewModel =
                            viewModel(factory = ViewModelFactory(NotesRepositoryImpl(PinApi.pinApiService)))
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        when (uiState) {
                            is NotesUIState.Loading -> {
                                //progressBar.visibility = View.VISIBLE
                            }
                            is NotesUIState.Success -> {
                                val newItems = (uiState as NotesUIState.Success<LiveData<List<ObjectWithNote>>>).data.observeAsState().value

                                newItems?.let { content ->
                                    Log.d("MainActivity", "size = ${content.size}")
                                    LazyColumn {
                                        content.let { it ->
                                            items(newItems.size) { noteIndex ->
                                                it[noteIndex].let { note ->
                                                    NoteListItem(
                                                        note.note, {},
                                                        toggleFavorite = {
                                                            viewModel.setFavorite(
                                                                note.container.uuid,
                                                                !note.container.favorite
                                                            )
                                                        },
                                                        toggleSelection = {
                                                            viewModel.toggleSelectedNote(note.note.uuid)
                                                        },
                                                        isSelected = uiState.selectedNotes.contains(
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

                            is NotesUIState.Error -> {
                                Toast.makeText(
                                    this,
                                    "Error: " + (uiState as NotesUIState.Error).error,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }
}
