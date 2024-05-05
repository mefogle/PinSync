package com.pinsync

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pinsync.api.PinApi
import com.pinsync.data.NotesRepositoryImpl
import com.pinsync.ui.AuthDialog
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
                        val content by viewModel.getNotes().observeAsState()
                        content?.let {
                            when (content?.status) {
                                Status.SUCCESS -> {
                                    //progressBar.visibility = View.GONE
                                    Log.d("MainActivity", "size = $content.content.size")
                                    LazyColumn {
                                        items(content?.data?.content!!.size) { note ->
                                            OutlinedCard(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .fillMaxWidth(), // Adjust padding as needed
                                                shape = MaterialTheme.shapes.medium, // Default shape, can be customized
                                            ) {
                                                // Occasionally null values seems to creep through and crash.  Not sure why.
                                                content?.data?.content!![note]?.let {
                                                    (it.data as PinApi.NoteData)?.let {
                                                        Column {
                                                            Text(
                                                                text = it.note.title,
                                                                style = MaterialTheme.typography.headlineSmall, // Title typeface
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(
                                                                    top = 8.dp,
                                                                    bottom = 8.dp,
                                                                    start = 4.dp,
                                                                    end = 4.dp
                                                                )
                                                            )
                                                            Text(
                                                                text = it.note.text,
                                                                style = MaterialTheme.typography.bodyMedium, // Smaller text typeface
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(
                                                                    top = 8.dp,
                                                                    bottom = 8.dp,
                                                                    start = 4.dp,
                                                                    end = 4.dp
                                                                ) // Space between title and text
                                                            )
                                                            Text(
                                                                text = DateFormat.getDateFormat(LocalContext.current)
                                                                    .format(it.createdAt),
                                                                style = MaterialTheme.typography.bodySmall, // Smaller text typeface
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(
                                                                    top = 4.dp,
                                                                    bottom = 8.dp,
                                                                    start = 4.dp,
                                                                    end = 4.dp
                                                                ) // Space between title and text
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //recyclerView.visibility = View.VISIBLE
                                }

                                Status.LOADING -> {
                                    //Do we need a skeleton loader here?
                                }

                                Status.ERROR -> {
                                    //Handle Error
                                    //progressBar.visibility = View.GONE
                                    Toast.makeText(this, content?.message, Toast.LENGTH_LONG).show()
                                }

                                else -> {}
                            }

                        }
                    }
                }
            }
        }
    }
}
