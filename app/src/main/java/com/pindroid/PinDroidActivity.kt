package com.pindroid

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pindroid.api.PinApi
import com.pindroid.ui.AuthDialog
import com.pindroid.ui.theme.PinDroidTheme
import com.pindroid.viewmodel.NotesViewModel
import com.pindroid.viewmodel.ViewModelFactory

abstract class PinDroidActivity : ComponentActivity() {

    companion object {
        lateinit var viewModel: NotesViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinDroidTheme {
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
                            viewModel(factory = ViewModelFactory(PinDroidApplication.notesRepository()))
                        val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
                        if (!uiState.loading) {
                            if (uiState.error == null)
                                ActivityBody()
                            else {
                                Toast.makeText(
                                    this,
                                    stringResource(R.string.error_toast_label) + uiState.error,
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
