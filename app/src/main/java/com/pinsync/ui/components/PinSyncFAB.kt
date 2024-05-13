package com.pinsync.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun PinSyncFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add",
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}