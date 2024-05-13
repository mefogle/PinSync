package com.pinsync.ui.navigation

object PinSyncRoute {
    const val NOTES = "notes"
    const val NOTE_DETAIL = "note_detail"
}

//data class PinSyncTopLevelDestination(
//    val route : String
//)

//class PinSyncNavigationActions (private val navController: NavHostController) {
//    fun navigateTo (destination: PinSyncTopLevelDestination) {
//        navController.navigate(destination.route) {
//            // Pop up to the start destination of the graph to
//            // avoid building up a large stack of destinations
//            popUpTo(navController.graph.findStartDestination().id) {
//                saveState = true
//            }
//            // Only one copy of each destination
//            launchSingleTop = true
//            // Remember state of previously selected items
//            restoreState = true
//        }
//    }
//}

//val TOP_LEVEL_DESTINATIONS = listOf(
//    PinSyncTopLevelDestination(
//        route = PinSyncRoute.NOTES
//    ),
//)