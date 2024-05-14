package com.pindroid

import android.content.Intent
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import com.pindroid.data.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class ShareActivity : PinDroidActivity() {

    @Composable
    override fun ActivityBody() {
        // Gather up any information that could be related to a sharing intent
        val isShared: Boolean = (intent.action == Intent.ACTION_SEND)
        val sharedText: String?
        var sharedTitle: String?
        if (isShared) {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: "No text shared"
            sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
            if (sharedTitle == null) {
                sharedTitle = "Shared at " + DateFormat.getDateFormat(this).format(
                    Date()
                ) + " " + DateFormat.getTimeFormat(this).format(Date())
            }
            LaunchedEffect(Unit) {
                lifecycleScope.launch(Dispatchers.IO) {
                    PinDroidApplication.notesRepository()
                        .createNote(Note(title = sharedTitle, text = sharedText))
                    finish()
                }
            }
            // Let the user know what we're doing.  The dialog will automatically dismiss itself
            // when the creation is finished.
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = "Creating note...",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Title : $sharedTitle",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Text : $sharedText",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {

                }
            )
        } else {
            finish()
        }
    }
}