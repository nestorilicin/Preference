package etf.nestor.preference.ui.navigation

sealed class Screen(val route: String) {
    data object Menu : Screen("menu")
    data object SingleplayerIntro : Screen("singleplayer_intro")
    data object Game : Screen("game")
    data object GameContinue : Screen("game_continue")
    data object Settings : Screen("settings")
}
