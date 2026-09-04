package etf.nestor.preference.ui.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.game.GameSnapshot
import etf.nestor.preference.ui.stateholders.SingleplayerViewModel

@Composable
fun ScoreBar(
    state: GameSnapshot,
    viewModel: SingleplayerViewModel,
    compact: Boolean = false,
) {
    val youFallback = stringResource(R.string.player_name_you)
    val names = listOf(
        state.playerNames.getOrElse(0) { youFallback },
        stringResource(R.string.player_name_bot1),
        stringResource(R.string.player_name_bot2)
    )
    // Levo bot koji sedi levo od igrača (2), u sredini igrač (0), desno bot koji sedi desno (1)
    val order = listOf(2, 0, 1)
    val sheetDialogPlayer by viewModel.sheetDialogPlayer.collectAsState()

    // Profilna slika i država — prikazuju se samo uz igrača (poziciju 0)
    val avatarPath by viewModel.avatarPath.collectAsState()
    val country by viewModel.country.collectAsState()
    val avatarBitmap = remember(avatarPath) {
        avatarPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 8.dp else 16.dp,
                    vertical = if (compact) 2.dp else 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                order.forEach { i ->
                    val sheet = state.scoreSheets[i]
                    val center = sheet?.center ?: 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.openScoreSheet(i) }
                    ) {
                        if (i == 0 && avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(if (compact) 20.dp else 28.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Text(
                            text = names[i],
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (i == 0 && country != null) {
                            Text(
                                text = country.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "$center",
                            style = if (compact) MaterialTheme.typography.labelLarge
                                    else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (center <= 0)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )

                    }
                }
            }
        }
    }

    // Dialog za score sheet
    val dialogPlayer = sheetDialogPlayer
    if (dialogPlayer != null) {
        ScoreSheetDialog(
            playerPosition = dialogPlayer,
            sheet = state.scoreSheets[dialogPlayer],
            allNames = names,
            onDismiss = { viewModel.closeScoreSheet() }
        )
    }
}
