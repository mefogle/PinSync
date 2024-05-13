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
    viewModel: NotesViewModel,
//    closeDetailScreen: () -> Unit
) {
    val navController = rememberNavController()
//    val navigationActions = remember(navController) {
//        PinSyncNavigationActions(navController)
//    }
 //   val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val selectedDestination =
//        navBackStackEntry?.destination?.route ?: PinSyncRoute.NOTES

    SyncAppContent(
        modifier = Modifier,
        viewModel = viewModel,
        navController = navController,
//        selectedDestination = selectedDestination,
//        navigateToTopLevelDestination = navigationActions::navigateTo,
//        closeDetailScreen = closeDetailScreen
    )
}

@Composable
fun SyncAppContent(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel,
    navController: NavHostController,
//    selectedDestination: String,
//    navigateToTopLevelDestination: (PinSyncTopLevelDestination) -> Unit,
    //    closeDetailScreen: () -> Unit
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
 //               closeDetailScreen = closeDetailScreen,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PinSyncNavHost(
    navController: NavHostController,
    viewModel: NotesViewModel,
//    closeDetailScreen: () -> Unit,
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
//                closeDetailScreen = closeDetailScreen,
//                navigateToDetail = {},
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
