package etf.nestor.preference.ui.screen.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.game.GameSnapshot

@Composable
fun ScoreBar(state: GameSnapshot) {
    val names = listOf(
        stringResource(R.string.player_name_you),
        stringResource(R.string.player_name_bot1),
        stringResource(R.string.player_name_bot2)
    )
    var sheetDialogPlayer by remember { mutableStateOf<Int?>(null) }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            names.forEachIndexed { i, name ->
                val sheet = state.scoreSheets[i]
                val center = sheet?.center ?: 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { sheetDialogPlayer = i }
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (center <= 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.tricks_count, state.tricksWon[i]),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    // Dialog za score sheet
    val dialogPlayer = sheetDialogPlayer
    if (dialogPlayer != null) {
        ScoreSheetDialog(
            playerName = names[dialogPlayer],
            sheet = state.scoreSheets[dialogPlayer],
            allNames = names,
            onDismiss = { sheetDialogPlayer = null}
        )
    }
}

