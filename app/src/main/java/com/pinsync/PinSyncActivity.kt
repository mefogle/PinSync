package com.pinsync

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pinsync.api.PinApi
import com.pinsync.ui.AuthDialog
import com.pinsync.ui.theme.PinSyncTheme
import com.pinsync.viewmodel.NotesViewModel
import com.pinsync.viewmodel.ViewModelFactory

abstract class PinSyncActivity : ComponentActivity() {

    companion object {
        lateinit var viewModel: NotesViewModel
    }

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
                            viewModel(factory = ViewModelFactory(PinSyncApplication.notesRepository()))
                        val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
                        if (!uiState.loading) {
                            if (uiState.error == null)
                                ActivityBody()
                            else {
                                Toast.makeText(
                                    this,
                                    "Error: " + uiState.error,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    abstract fun ActivityBody()
}
