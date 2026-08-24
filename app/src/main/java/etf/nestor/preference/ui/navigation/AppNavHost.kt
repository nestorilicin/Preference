package etf.nestor.preference.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import etf.nestor.preference.ui.screen.MenuScreen
import etf.nestor.preference.ui.screen.SettingsScreen
import etf.nestor.preference.ui.singleplayer.SingleplayerIntroScreen
import etf.nestor.preference.ui.singleplayer.SinglePlayerGameScreen
import etf.nestor.preference.ui.singleplayer.SingleplayerStartMode

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
                onContinue = { navController.navigate(Screen.GameContinue.route) }
            )
        }

        composable(Screen.Game.route) {
            SinglePlayerGameScreen(startMode = SingleplayerStartMode.NEW)
        }

        composable(Screen.GameContinue.route) {
            SinglePlayerGameScreen(startMode = SingleplayerStartMode.CONTINUE)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
