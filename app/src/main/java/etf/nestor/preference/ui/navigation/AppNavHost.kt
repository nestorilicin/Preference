package etf.nestor.preference.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import etf.nestor.preference.ui.screen.MenuScreen
import etf.nestor.preference.ui.screen.SettingsScreen
import etf.nestor.preference.ui.screen.SinglePlayerGameScreen

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController, startDestination = Screen.Menu.route
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onSingleplayerClick = {
                    navController.navigate(Screen.Game.route)
                },
                onMultiplayerClick = {

                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Game.route) {
            SinglePlayerGameScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
