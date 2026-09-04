package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R
import etf.nestor.preference.ui.composables.PlayerNameDialog
import etf.nestor.preference.ui.stateholders.SettingsViewModel


@Composable
fun MenuScreen(
    onSingleplayerClick: () -> Unit,
    onMultiplayerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val playerName by settingsViewModel.playerName.collectAsState()
    // TODO PROMENI EKRANE
    AppScreen(title = stringResource(R.string.app_name)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onSingleplayerClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.main_singleplayer))
            }

            Button(
                onClick = onMultiplayerClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.main_multiplayer))
            }

            Button(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.main_settings))
            }
        }
    }

    // Pri prvom pokretanju aplikacije igrač mora da unese ime pre nego što nastavi
    if (playerName.isBlank()) {
        PlayerNameDialog(onConfirm = { settingsViewModel.setPlayerName(it) })
    }
}
