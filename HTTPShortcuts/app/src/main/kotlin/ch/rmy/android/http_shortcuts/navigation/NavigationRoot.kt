package ch.rmy.android.http_shortcuts.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ch.rmy.android.http_shortcuts.activities.importexport.ImportExportScreen

@Composable
fun NavigationRoot() {
    val navController = rememberNavController()
    NavigationEventHandler(navController)

    NavHost(
        navController = navController,
        startDestination = NavigationDestination.ImportExport.routePattern,
    ) {
        composable(NavigationDestination.ImportExport) { backStackEntry ->
            ImportExportScreen()
        }
    }
}
