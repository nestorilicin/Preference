package etf.nestor.preference.ui.screen.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R
import etf.nestor.preference.domain.game.GameSnapshot

@Composable
fun TrickArea(state: GameSnapshot) {
    val names = listOf(
        stringResource(R.string.player_name_you),
        stringResource(R.string.player_name_bot1),
        stringResource(R.string.player_name_bot2)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.label_current_trick),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (0..2).forEach { pos ->
                val playedCard = state.currentTrick.find { it.playerPosition == pos }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = names[pos],
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(2.dp))
                    if (playedCard != null) {
                        CardFront(
                            card = playedCard.card,
                            modifier = Modifier.size(52.dp, 78.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp, 78.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}
