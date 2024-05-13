package com.pinsync

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.pinsync.data.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class ShareActivity : PinSyncActivity() {

    @Composable
    override fun activityBody() {
        // Gather up any information that could be related to a sharing intent
        val isShared : Boolean = (intent.action == Intent.ACTION_SEND)
        var sharedText : String? = null
        var sharedTitle : String? = null
        if (isShared) {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: "No text shared"
            sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE)
            if (sharedTitle == null) {
                sharedTitle = "Shared at " + DateFormat.getDateFormat(this).format(
                    Date()
                ) + " " + DateFormat.getTimeFormat(this).format(Date())
            }
            LaunchedEffect(Unit) {
                lifecycleScope.launch (Dispatchers.IO){
                    PinSyncApplication.notesRepository()
                        .createNote(Note(title = sharedTitle, text = sharedText))
                    finish()
                }
            }
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
        }
        else {
            finish()
        }
    }
}