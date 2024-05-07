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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pinsync.api.PinApi
import com.pinsync.data.NotesRepositoryImpl
import com.pinsync.ui.AuthDialog
import com.pinsync.ui.components.NoteListItem
import com.pinsync.ui.theme.PinSyncTheme
import com.pinsync.util.Status
import com.pinsync.viewmodel.NotesViewModel
import com.pinsync.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: NotesViewModel

    @Suppress("UNNECESSARY_SAFE_CALL") // Needed for the null Data check down below.
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
                        //val notesContent by viewModel.getNotes().observeAsState()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        if (uiState.error != null) {
                            Toast.makeText(this, "Error: " + uiState.error, Toast.LENGTH_LONG)
                                .show()
                        } else if (!uiState.loading) {
                            uiState.notes?.values.let { content ->
                                //progressBar.visibility = View.GONE
                                Log.d("MainActivity", "size = $content.content.size")
                                LazyColumn {
                                    items(content!!.size) { noteIndex ->
                                        // Occasionally null values seems to creep through and crash.  Not sure why.
                                        content!!.elementAt(noteIndex)?.let { note ->
                                            NoteListItem(
                                                note.data as PinApi.NoteData, {},
                                                toggleFavorite = {
                                                    viewModel.setFavorite(
                                                        note.uuid,
                                                        !note.favorite
                                                    )
                                                },
                                                toggleSelection = {
                                                    viewModel.toggleSelectedNote(note.uuid)
                                                },
                                                isSelected = uiState.selectedNotes.contains(note.uuid),
                                                isFavorite = note.favorite
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
