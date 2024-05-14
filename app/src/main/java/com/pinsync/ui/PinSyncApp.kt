package com.pinsync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pinsync.ui.navigation.PinSyncRoute
import com.pinsync.viewmodel.NotesViewModel

@Composable
fun PinSyncApp(
    viewModel: NotesViewModel,
//    closeDetailScreen: () -> Unit = {}
) {
    PinSyncNavigationWrapper(
        viewModel = viewModel,
//        closeDetailScreen = closeDetailScreen
    )
}

@Composable
private fun PinSyncNavigationWrapper(
    viewModel: NotesViewModel
) {
    val navController = rememberNavController()

    SyncAppContent(
        modifier = Modifier,
        viewModel = viewModel,
        navController = navController,
    )
}

@Composable
fun SyncAppContent(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel,
    navController: NavHostController,
) {
    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            PinSyncNavHost(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PinSyncNavHost(
    navController: NavHostController,
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = PinSyncRoute.NOTES,
    ) {
        composable(PinSyncRoute.NOTES) {
            NotesListScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        composable(PinSyncRoute.NOTE_DETAIL) {
            NoteDetailScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }
}
