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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pinsync.viewmodel.NoteUIState
import com.pinsync.viewmodel.NotesViewModel
import com.pinsync.viewmodel.ObjectWithNoteLiveData

@Composable
fun NoteDetailScreen(
    viewModel: NotesViewModel,
//    title: String,
//    notes: String,
//    isEditing: Boolean,
//    onEditButtonClick: () -> Unit,
//    onDeleteButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
        val detail = (uiState as NoteUIState.Success<ObjectWithNoteLiveData>).data.observeAsState().value
        // Title
        Text(
            text = detail?.note?.note?.title ?: "",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        // Notes
        OutlinedTextField(
            value = detail?.note?.note?.text ?: "",
            onValueChange = {}, // Disable editing in this composable
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            enabled = false // Disable editing in this composable
        )

        // Edit/Save button
        Button(
            onClick = { } ,
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .align(alignment = Alignment.End)
        ) {
            Text(text = if (true) "Save" else "Edit")
        }

        // Delete button
        IconButton(
            onClick = { },
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
}