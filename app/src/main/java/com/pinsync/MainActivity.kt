package com.pinsync

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pinsync.ui.theme.PinSyncTheme

class MainActivity : ComponentActivity() {
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
                    if (!PinAPI.isAuthenticated() && showDialog) {
                        AuthDialog(onDismissRequest = {showDialog = false})
                    } else {
                        val myViewModel: ContentViewModel = viewModel()
                        val content by myViewModel.data.observeAsState()
                        content?.let {
                            Log.d("MainActivity", "size = $it.content.size")
                            LazyColumn {
                                items(it.content.size) { note ->
                                    OutlinedCard(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(), // Adjust padding as needed
                                        shape = MaterialTheme.shapes.medium, // Default shape, can be customized
                                    ) {
                                        Column {
                                            Text(
                                                text = it.content[note].data.note.title,
                                                style = MaterialTheme.typography.headlineSmall, // Title typeface
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 4.dp, end = 4.dp)
                                            )
                                            Text(
                                                text = it.content[note].data.note.text,
                                                style = MaterialTheme.typography.bodyMedium, // Smaller text typeface
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 4.dp, end = 4.dp) // Space between title and text
                                            )
                                            Text(
                                                text = DateFormat.getDateFormat(this@MainActivity).format(it.content[note].data.createdAt),
                                                style = MaterialTheme.typography.bodySmall, // Smaller text typeface
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp, start = 4.dp, end = 4.dp) // Space between title and text
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
