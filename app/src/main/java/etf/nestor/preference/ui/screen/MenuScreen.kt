package etf.nestor.preference.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R
import etf.nestor.preference.ui.common.PlayerNameDialog
import etf.nestor.preference.ui.viewmodel.SettingsViewModel


@Composable
fun MenuScreen(
    onSingleplayerClick: () -> Unit,
    onMultiplayerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val playerName by settingsViewModel.playerName.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = onSingleplayerClick
        ) {
            Text(stringResource(R.string.main_singleplayer))
        }

        Button(
            onClick = onMultiplayerClick
        ) {
            Text(stringResource(R.string.main_multiplayer))
        }

        Button(
            onClick = onSettingsClick
        ) {
            Text(stringResource(R.string.main_settings))
        }
    }

    // Pri prvom pokretanju aplikacije igrač mora da unese ime pre nego što nastavi
    if (playerName.isBlank()) {
        PlayerNameDialog(onConfirm = { settingsViewModel.setPlayerName(it) })
    }
}
