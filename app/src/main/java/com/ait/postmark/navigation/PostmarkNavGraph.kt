package com.ait.postmark.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ait.postmark.auth.AuthRepository
import com.ait.postmark.ui.entry.EntryDetailScreen
import com.ait.postmark.ui.entry.NewEntryScreen
import com.ait.postmark.ui.list.ListScreen
import com.ait.postmark.ui.login.LoginScreen
import com.ait.postmark.ui.map.MapScreen

object Routes {
    const val LOGIN = "login"
    const val LIST = "list"
    const val MAP = "map"
    const val NEW_ENTRY = "newEntry"
    const val ENTRY_DETAIL = "entry/{entryId}"
    fun entryDetail(id: String) = "entry/$id"
}

@Composable
fun PostmarkNavGraph(authRepo: AuthRepository = remember { AuthRepository() }) {
    val navController = rememberNavController()
    val user by authRepo.authState.collectAsState(initial = authRepo.currentUser)

    // The start destination depends on whether we have a logged-in user.
    // Firebase Auth restores the session from disk on app launch.
    val start = if (user != null) Routes.LIST else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoggedIn = {
                navController.navigate(Routes.LIST) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.LIST) {
            ListScreen(
                onOpenEntry = { id -> navController.navigate(Routes.entryDetail(id)) },
                onNewEntry = { navController.navigate(Routes.NEW_ENTRY) },
                onSwitchToMap = { navController.navigate(Routes.MAP) { popUpTo(Routes.LIST) { inclusive = true } } },
                onSignOut = {
                    authRepo.signOut()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                }
            )
        }
        composable(Routes.MAP) {
            MapScreen(
                onOpenEntry = { id -> navController.navigate(Routes.entryDetail(id)) },
                onNewEntry = { navController.navigate(Routes.NEW_ENTRY) },
                onSwitchToList = { navController.navigate(Routes.LIST) { popUpTo(Routes.MAP) { inclusive = true } } }
            )
        }
        composable(Routes.NEW_ENTRY) {
            NewEntryScreen(onDone = { navController.popBackStack() })
        }
        composable(Routes.ENTRY_DETAIL) { backStack ->
            val id = backStack.arguments?.getString("entryId").orEmpty()
            EntryDetailScreen(entryId = id, onBack = { navController.popBackStack() })
        }
    }
}
