package com.pinsync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pinsync.viewmodel.NotesViewModel

@Composable
fun NoteDetailScreen(
    navController: NavController,
    viewModel: NotesViewModel,
//    title: String,
//    notes: String,
//    isEditing: Boolean,
//    onEditButtonClick: () -> Unit,
//    onDeleteButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    if (!uiState.loading) {
        if (uiState.error == null) {
            Column(modifier = modifier) {
                val detail = uiState.currentNote
                if (uiState.isDetailOnlyOpen) {
                    var title by remember { mutableStateOf( detail?.note?.note?.title ?: "") }
                    var text by remember { mutableStateOf( detail?.note?.note?.text ?: "") }


                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { newTitle -> title = newTitle},
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        label = { Text(text = "Title") },
                        enabled = true
                    )

                    // Notes
                    OutlinedTextField(
                        value = text,
                        onValueChange = { newText -> text = newText},
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = { Text(text = "Text") },
                        enabled = true // Disable editing in this composable
                    )

                    // Edit/Save button
                    Button(
                        onClick = { detail?.let {
                            it.note.note.title = title
                            it.note.note.text = text
                            viewModel.updateNote(it) }},
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .align(alignment = Alignment.End)
                    ) {
                        Text(text = if (true) "Save" else "Edit")
                    }

                    // Delete button
                    IconButton(
                        onClick = { detail?.let { viewModel.deleteNote(it) } },
                        modifier = Modifier
                            .padding(top = 16.dp, end = 16.dp)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
                else {
                    // Item has been saved or deleted, so navigate up
                   LaunchedEffect(Unit) {
                        navController.navigateUp()
                   }
                }
            }
        }
   }
}