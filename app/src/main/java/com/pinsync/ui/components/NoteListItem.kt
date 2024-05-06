package com.pinsync.ui.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pinsync.api.PinApi
import java.util.UUID

@OptIn(
    ExperimentalFoundationApi::class
)
@Composable
fun NoteListItem(
    noteData: PinApi.NoteData,
    navigateToDetail: (UUID) -> Unit,
    toggleSelection: (UUID) -> Unit,
    toggleFavorite: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isFavorite: Boolean = false
) {
    noteData.let {
        Card(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .semantics { selected = isSelected }
                .clip(CardDefaults.shape)
                .combinedClickable(
                    onClick = { navigateToDetail(it.uuid) },
                    onLongClick = { toggleSelection(it.uuid) }
                )
                .clip(CardDefaults.shape),
                colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val clickModifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { toggleSelection(it.uuid) }
                    AnimatedContent(targetState = isSelected, label = "avatar") { selected ->
                        if (selected) {
                            SelectedImage(clickModifier)
                        } else {
                            UnselectedImage(clickModifier)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = it.note.title,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = it.note.text,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = DateFormat.getDateFormat(LocalContext.current)
                                .format(it.createdAt)
                                    + " " +
                                    DateFormat.getTimeFormat(LocalContext.current)
                                        .format(it.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    IconButton(
                        onClick = { toggleFavorite(it.uuid) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedImage(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun UnselectedImage(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    ) {
//        Icon(
//            Icons.Default.Check,
//            contentDescription = null,
//            modifier = Modifier
//                .size(24.dp)
//                .align(Alignment.Center),
//            tint = MaterialTheme.colorScheme.onPrimary
//        )
    }
}