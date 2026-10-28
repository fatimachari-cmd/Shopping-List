package com.fatimachari.listadecompra.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fatimachari.listadecompra.ui.theme.listdetail.ListDetailScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "detalle_lista/1") {
        composable("detalle_lista/{listId}") { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            ListDetailScreen(
                listId = listId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
