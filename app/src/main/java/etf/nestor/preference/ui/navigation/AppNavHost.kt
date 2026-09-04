package etf.nestor.preference.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import etf.nestor.preference.ui.screens.MenuScreen
import etf.nestor.preference.ui.screens.SettingsScreen
import etf.nestor.preference.ui.screens.SavedGamesScreen
import etf.nestor.preference.ui.screens.SingleplayerIntroScreen
import etf.nestor.preference.ui.screens.SinglePlayerGameScreen
import etf.nestor.preference.ui.screens.SingleplayerStartMode

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController, startDestination = Screen.Menu.route
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onSingleplayerClick = {
                    navController.navigate(Screen.SingleplayerIntro.route)
                },
                onMultiplayerClick = {

                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.SingleplayerIntro.route) {
            SingleplayerIntroScreen(
                onNewGame = { navController.navigate(Screen.Game.route) },
                onContinue = { navController.navigate(Screen.GameContinue.route) },
                onSavedGames = { navController.navigate(Screen.SavedGames.route) }
            )
        }

        composable(Screen.Game.route) {
            SinglePlayerGameScreen(
                startMode = SingleplayerStartMode.NEW,
                onExit = { navController.popBackStack() }
            )
        }

        composable(Screen.GameContinue.route) {
            SinglePlayerGameScreen(
                startMode = SingleplayerStartMode.CONTINUE,
                onExit = { navController.popBackStack() }
            )
        }

        composable(
            Screen.GameLoad.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")
            if (id != null) {
                SinglePlayerGameScreen(
                    startMode = SingleplayerStartMode.LOAD,
                    loadId = id,
                    // Izlazak odavde je uvek tih (partija se prepiše bez dijaloga — vidi
                    // GamePlayScreen), pa se vraća pravo na uvodni ekran, preskačući
                    // "Sačuvane partije" na koje se inače ne bi imalo smisla vratiti.
                    onExit = { navController.popBackStack(Screen.SingleplayerIntro.route, false) }
                )
            }
        }

        composable(Screen.SavedGames.route) {
            SavedGamesScreen(
                onBack = { navController.popBackStack() },
                onLoad = { id -> navController.navigate(Screen.GameLoad.createRoute(id)) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
