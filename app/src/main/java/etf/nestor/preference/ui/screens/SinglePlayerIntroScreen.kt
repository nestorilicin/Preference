package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel

@Composable
fun SingleplayerIntroScreen(
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onSavedGames: () -> Unit,
    viewModel: SingleplayerViewModel = hiltViewModel()
) {
    val hasSavedGame by viewModel.hasSavedGame.collectAsState()

    AppScreen(title = stringResource(R.string.subtitle_singleplayer)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasSavedGame) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.btn_continue_game))
                }
            }

            OutlinedButton(
                onClick = onNewGame,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.btn_new_game))
            }

            OutlinedButton(
                onClick = onSavedGames,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.btn_saved_games))
            }
        }
    }
}
