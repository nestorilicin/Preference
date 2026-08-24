package etf.nestor.preference.ui.singleplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R

/**
 * Ekran pre početka singleplayer partije: Nastavi (ako postoji sačuvana partija) ili Nova partija.
 */
@Composable
fun SingleplayerIntroScreen(
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    viewModel: SingleplayerViewModel = hiltViewModel()
) {
    val hasSavedGame = viewModel.hasSavedGame

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.subtitle_singleplayer),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(40.dp))

        if (hasSavedGame) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.btn_continue_game))
            }
            Spacer(Modifier.height(16.dp))
        }

        OutlinedButton(
            onClick = onNewGame,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Text(stringResource(R.string.btn_new_game))
        }
    }
}
