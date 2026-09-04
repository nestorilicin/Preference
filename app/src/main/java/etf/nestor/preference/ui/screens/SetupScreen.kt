package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.ui.composables.SimpleSpinner
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel

@Composable
fun SetupScreen(viewModel: SingleplayerViewModel) {
    val selectedScore by viewModel.selectedStartScore.collectAsState()

    AppScreen(title = stringResource(R.string.subtitle_singleplayer)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.label_starting_score),
                style = MaterialTheme.typography.titleMedium
            )

            SimpleSpinner(
                value = selectedScore,
                options = viewModel.startScoreOptions,
                displayText = { stringResource(R.string.score_option, it) },
                onSelect = { viewModel.selectStartScore(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.startGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    stringResource(R.string.btn_start_game),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
