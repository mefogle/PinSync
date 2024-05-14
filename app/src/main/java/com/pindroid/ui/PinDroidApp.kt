package com.pindroid.ui

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
import com.pindroid.ui.navigation.PinDroidRoute
import com.pindroid.viewmodel.NotesViewModel

@Composable
fun PinDroidApp(
    viewModel: NotesViewModel,
//    closeDetailScreen: () -> Unit = {}
) {
    PinDroidNavigationWrapper(
        viewModel = viewModel,
//        closeDetailScreen = closeDetailScreen
    )
}

@Composable
private fun PinDroidNavigationWrapper(
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
            PinDroidNavHost(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PinDroidNavHost(
    navController: NavHostController,
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = PinDroidRoute.NOTES,
    ) {
        composable(PinDroidRoute.NOTES) {
            NotesListScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        composable(PinDroidRoute.NOTE_DETAIL) {
            NoteDetailScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }
}
