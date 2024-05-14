package com.pindroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pindroid.R
import com.pindroid.viewmodel.NotesViewModel

@Composable
fun NoteDetailScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    if (!uiState.loading) {
        if (uiState.error == null) {
            Column(
                modifier = modifier
                    .verticalScroll(rememberScrollState(), reverseScrolling = true)
                    .fillMaxSize()
                    .imePadding()
            ) {
                val detail = uiState.currentNote
                if (uiState.isDetailOnlyOpen) {
                    var title by remember { mutableStateOf(detail?.note?.note?.title ?: "") }
                    var text by remember { mutableStateOf(detail?.note?.note?.text ?: "") }


                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { newTitle -> title = newTitle },
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        label = { Text(text = stringResource(R.string.title)) },
                        enabled = true
                    )

                    // Notes
                    OutlinedTextField(
                        value = text,
                        onValueChange = { newText -> text = newText },
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .height(IntrinsicSize.Min),
                        label = { Text(text = stringResource(R.string.text)) },
                        enabled = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Delete button
                        IconButton(
                            onClick = { detail?.let { viewModel.deleteNote(it) } },
                            modifier = Modifier
                                .padding(top = 16.dp, end = 16.dp, start = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }

                        // Push the other buttons to the right
                        Spacer(Modifier.weight(1f, true))

                        // Cancel button
                        OutlinedButton(
                            onClick = {
                                navController.navigateUp()
                            },
                            modifier = Modifier
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Text(text = stringResource(R.string.cancel))
                        }

                        // Save button
                        Button(
                            onClick = {
                                detail?.let {
                                    it.note.note.title = title
                                    it.note.note.text = text
                                    viewModel.updateNote(it)
                                }
                            },
                            modifier = Modifier
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Text(text = stringResource(R.string.save))
                        }
                    }

                } else {
                    // Item has been saved or deleted, so navigate up
                    LaunchedEffect(Unit) {
                        navController.navigateUp()
                    }
                }
            }
        }
    }
}