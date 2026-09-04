package etf.nestor.preference.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import etf.nestor.preference.R
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Lista ručno sačuvanih partija (sa imenom koje je igrač uneo pri izlasku) — učitaj ili obriši. */
@Composable
fun SavedGamesScreen(
    onBack: () -> Unit,
    onLoad: (Long) -> Unit,
    viewModel: SingleplayerViewModel = hiltViewModel()
) {
    val savedGames by viewModel.savedGames.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault()) }

    AppScreen(title = stringResource(R.string.saved_games_title)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            if (savedGames.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_saved_games),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedGames, key = { it.id }) { save ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(save.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = dateFormat.format(Date(save.savedAt)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { onLoad(save.id) }) {
                                    Text(stringResource(R.string.btn_load_game))
                                }
                                TextButton(onClick = { viewModel.deleteSavedGame(save.id) }) {
                                    Text(stringResource(R.string.btn_delete_game))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.btn_back))
            }
        }
    }
}
